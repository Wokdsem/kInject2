package com.wokdsem.kinject.compiler

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

private const val COMPANION_GRAPH_PARAM = "graph"
private const val OVERRIDE_PROVIDERS = "Providers"
private const val OVERRIDE_GRAPH_PARAM = "overrideWith"
private const val OVERRIDE_PARAM = "_P"

private val suppress = listOf(
    "RedundantVisibilityModifier", "RemoveRedundantBackticks", "RemoveRedundantQualifierName", "PrivatePropertyName"
)

internal fun generate(graph: Graph, codeGenerator: CodeGenerator) {
    val blueprint = Blueprint(graph)
    FileSpec.builder(blueprint.rootPackage, blueprint.contractName)
        .addAnnotation(AnnotationSpec.builder(Suppress::class).apply { suppress.onEach { addMember("%S", it) } }.build())
        .addType(getContract(blueprint))
        .addType(getRunner(blueprint))
        .build().writeTo(codeGenerator = codeGenerator, aggregating = false, originatingKSFiles = graph.files)
}

private fun getContract(blueprint: Blueprint): TypeSpec {
    return TypeSpec.interfaceBuilder(blueprint.contractName).apply {
        val contractVisibility = checkNotNull(blueprint.root.getVisibility().toKModifier())
        addModifiers(contractVisibility)
        blueprint.exports.forEach { export ->
            addProperty(propertySpec = PropertySpec.builder(name = blueprint.exportsNames.getValue(export.id), type = export.node.toTypeName(), ABSTRACT).build())
        }
        addType(
            typeSpec = TypeSpec.interfaceBuilder(OVERRIDE_PROVIDERS).apply {
                blueprint.providers.values.forEach { provider ->
                    val returnType = LambdaTypeName.get(returnType = provider.node.toTypeName()).copy(nullable = true)
                    addFunction(funSpec = FunSpec.builder(name = blueprint.providersNames.getValue(provider.id)).returns(returnType).addStatement("return null").build())
                }
            }.build()
        )
        addType(
            typeSpec = TypeSpec.companionObjectBuilder().addFunction(
                FunSpec.builder("from")
                    .addParameter(COMPANION_GRAPH_PARAM, blueprint.rootType)
                    .addParameter(ParameterSpec.builder(OVERRIDE_GRAPH_PARAM, blueprint.overrideType).defaultValue("%L", null).build())
                    .returns(blueprint.contractType).addStatement("return %T($COMPANION_GRAPH_PARAM, $OVERRIDE_GRAPH_PARAM)", blueprint.runnerType)
                    .build()
            ).build()
        )
    }.build()
}

private fun getRunner(blueprint: Blueprint): TypeSpec {
    fun getExportProperty(export: Export): PropertySpec {
        return PropertySpec.builder(name = blueprint.exportsNames.getValue(export.id), type = export.node.toTypeName(), PUBLIC, OVERRIDE).apply {
            when (val type = export.type) {
                Export.Type.Delegated -> getter(getter = FunSpec.getterBuilder().addStatement("return ${export.id.propertyName()}").build())
                is Export.Type.Bracket -> {
                    val superInterface = export.node.toTypeName()
                    val exportCode = "${blueprint.rootProperty}.${export.reference}()"
                    val properties = type.dependencies.map { dependency ->
                        PropertySpec.builder(dependency.name, dependency.node.toTypeName(), OVERRIDE).apply {
                            getter(getter = FunSpec.getterBuilder().addStatement("return ${dependency.id.propertyName()}").build())
                        }.build()
                    }
                    val exporterInstance = TypeSpec.anonymousClassBuilder().addSuperinterface(superInterface).apply { properties.forEach(::addProperty) }.build()
                    delegate(
                        codeBlock = CodeBlock.builder().beginControlFlow("lazy").addStatement(exportCode).add(format = "$exporterInstance\n").endControlFlow().build()
                    )
                }
            }
        }.build()
    }

    fun getModuleProperty(module: Module): PropertySpec {
        return PropertySpec.builder(module.id.moduleName(), module.node.toTypeName(), PRIVATE).apply {
            val propertyCode = "${module.source.moduleName()}.${module.reference}().get()"
            delegate(codeBlock = CodeBlock.builder().beginControlFlow("lazy").add(propertyCode).endControlFlow().build())
        }.build()
    }

    fun getProviderProperties(providers: Map<Id, Provider>): List<PropertySpec> {
        return mutableListOf<PropertySpec>().apply {
            val addedSpecs = mutableSetOf<Id>()
            fun addSpec(id: Id) {
                if (id in addedSpecs) return
                val provider = providers.getValue(id)
                provider.dependencies.forEach { addSpec(it.id) }
                add(
                    PropertySpec.builder(provider.id.propertyName(), provider.node.toTypeName(), PRIVATE).apply {
                        val dependencies = provider.dependencies.joinToString(separator = ",") { "${it.name} = ${it.id.propertyName()}" }
                        val propertyCode =
                            "$OVERRIDE_PARAM?.${blueprint.providersNames.getValue(id)}()?.invoke() ?: ${provider.source.moduleName()}.${provider.reference}($dependencies).get()"
                        when (provider.scope) {
                            Scope.FACTORY -> getter(getter = FunSpec.getterBuilder().addStatement("return $propertyCode").build())
                            Scope.EAGER -> initializer(format = propertyCode)
                            Scope.SINGLE -> delegate(codeBlock = CodeBlock.builder().beginControlFlow("lazy").add(propertyCode).endControlFlow().build())
                        }
                    }.build()
                )
                addedSpecs += id
            }
            providers.forEach { (id, _) -> addSpec(id) }
        }
    }

    return TypeSpec.classBuilder(blueprint.runnerName).apply {
        addModifiers(PRIVATE)
        addSuperinterface(blueprint.contractType)
        primaryConstructor(
            primaryConstructor = FunSpec.constructorBuilder().addParameter(blueprint.rootProperty, blueprint.rootType).addParameter(OVERRIDE_PARAM, blueprint.overrideType).build()
        )
        addProperty(propertySpec = PropertySpec.builder(blueprint.rootProperty, blueprint.rootType).initializer(blueprint.rootProperty).addModifiers(PRIVATE).build())
        addProperty(propertySpec = PropertySpec.builder(OVERRIDE_PARAM, blueprint.overrideType).initializer(OVERRIDE_PARAM).addModifiers(PRIVATE).build())
        blueprint.exports.forEach { export -> addProperty(propertySpec = getExportProperty(export)) }
        blueprint.modules.forEach { module -> addProperty(propertySpec = getModuleProperty(module)) }
        getProviderProperties(blueprint.providers).forEach(::addProperty)
    }.build()
}

private fun Id.propertyName(): String = "`DEP_${id.sanitize()}`"
private fun Id.moduleName(): String = "`MOD_${id.sanitize()}`"
private fun String.sanitize(): String {
    return asSequence().filter { it != '`' }.joinToString(separator = "") { char ->
        when (char) {
            '_' -> "__"
            '<', '>', '.' -> "_"
            else -> char.toString()
        }
    }
}

private fun List<Id>.name(): Map<Id, String> {
    val regex = """^([^<]+)\b(?:<(.+)>)?$""".toRegex()
    fun List<Id>.nameByGrade(grade: Int = 1): List<Pair<Id, String>> {
        fun String.onGrade(): String {
            var count = grade
            return takeLastWhile { it != '.' || --count > 0 }
        }

        fun String.name(): String {
            if (regex.matchEntire(this) == null) error(this)
            val (type, generic) = checkNotNull(regex.matchEntire(this)).destructured
            val typePart = type.onGrade().replace(".", "").replace("_", "__")
            val genericPart = generic.takeIf(String::isNotEmpty)?.let { "_of${it.split(", ").joinToString(separator = "_and", transform = String::name)}" }.orEmpty()
            return typePart + genericPart
        }
        return groupBy { id -> id.id.name().replaceFirstChar(Char::lowercase) }.entries.flatMap { (name, id) ->
            if (id.size == 1) listOf(element = id.first() to name) else id.nameByGrade(grade + 1)
        }
    }
    return nameByGrade().toMap()
}

private class Blueprint(graph: Graph) {
    val root = graph.root
    val rootPackage = root.packageName.asString()
    val rootType = root.toClassName()
    val rootProperty = Id(rootType.toString()).moduleName()
    val contractName = graph.name.takeUnless(String::isBlank) ?: "K${graph.root.simpleName.getShortName()}"
    val contractType = ClassName(rootPackage, contractName)
    val overrideType = contractType.nestedClass(OVERRIDE_PROVIDERS).copy(nullable = true)
    val runnerName = "_$contractName"
    val runnerType = ClassName(rootPackage, runnerName)
    val exports = graph.exports
    val modules = graph.modules
    val providers = graph.providers
    val exportsNames = exports.map(Export::id).name()
    val providersNames = providers.keys.toList().name()
}
