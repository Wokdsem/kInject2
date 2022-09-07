package com.wokdsem.kinject2p

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

private const val COMPANION_GRAPH_PARAM = "graph"

private val suppress = listOf(
    "RedundantSuppression", "PrivatePropertyName", "RemoveRedundantBackticks", "RedundantVisibilityModifier", "PropertyName", "RemoveRedundantQualifierName", "unused"
)

internal fun generate(graph: Graph, codeGenerator: CodeGenerator) {
    val root = graph.root
    val graphClass = root.toClassName()
    val graphVisibility = checkNotNull(root.getVisibility().toKModifier())
    val kGraphPackage = root.packageName.asString()
    val kGraphName = "K${root.simpleName.getShortName()}"
    val kGraphClass = ClassName(kGraphPackage, kGraphName)
    val graphProperty = Id(id = root.toClassName().toString()).moduleName()
    FileSpec.builder(kGraphPackage, kGraphName).addType(
        TypeSpec.classBuilder(kGraphName).apply {
            addModifiers(graphVisibility)
            addAnnotation(AnnotationSpec.builder(Suppress::class).apply { suppress.onEach { addMember("%S", it) } }.build())
            primaryConstructor(primaryConstructor = FunSpec.constructorBuilder().addParameter(graphProperty, graphClass).addModifiers(PRIVATE).build())
            addProperty(propertySpec = PropertySpec.builder(graphProperty, graphClass).initializer(graphProperty).addModifiers(PRIVATE).build())
            addType(typeSpec = getCompanionBuilder(graphClass, kGraphClass, graphVisibility))
            getExportedProperties(graphProperty, graph.exporters).forEach(::addProperty)
            graph.modules.forEach { module -> addProperty(propertySpec = getModuleProperty(module)) }
            graph.providers.forEach { provider -> addProperty(propertySpec = getProviderProperty(provider)) }
        }.build()
    ).build().writeTo(codeGenerator = codeGenerator, aggregating = false, originatingKSFiles = graph.files)
}

private fun getCompanionBuilder(graphClass: TypeName, kGraphClass: TypeName, visibility: KModifier): TypeSpec {
    return TypeSpec.companionObjectBuilder().addFunction(
        FunSpec.builder("from").addModifiers(visibility).addParameter(COMPANION_GRAPH_PARAM, graphClass)
            .returns(kGraphClass).addStatement("return %T($COMPANION_GRAPH_PARAM)", kGraphClass)
            .build()
    ).build()
}

private fun getExportedProperties(graphProperty: String, exporters: List<Exporter>): List<PropertySpec> {
    val regex = """^([^<]+)\b(?:<(.+)>)?$""".toRegex()
    fun List<Exporter>.name(grade: Int = 1): List<Pair<String, Exporter>> {
        fun String.onGrade(): String {
            var counter = grade
            return takeLastWhile { it != '.' || --counter > 0 }
        }

        fun String.name(): String {
            if (regex.matchEntire(this) == null) error(this)
            val (type, generic) = checkNotNull(regex.matchEntire(this)).destructured
            val typePart = type.onGrade().replace(".", "").replace("_", "__")
            val genericPart = generic.takeIf(String::isNotEmpty)?.let { "_of${it.split(", ").joinToString(separator = "_and", transform = String::name)}" }.orEmpty()
            return typePart + genericPart
        }
        return groupBy { exporter -> exporter.id.id.name().replaceFirstChar(Char::lowercase) }.entries.flatMap { (name, exporters) ->
            if (exporters.size == 1) listOf(element = name to exporters.first()) else exporters.name(grade + 1)
        }
    }
    return exporters.name().map { (name, exporter) ->
        PropertySpec.builder(name, exporter.node.toTypeName(), PUBLIC).apply {
            when (val type = exporter.type) {
                Exporter.Type.Delegated -> getter(getter = FunSpec.getterBuilder().addStatement("return ${exporter.id.propertyName()}").build())
                is Exporter.Type.Bracket -> {
                    val superInterface = exporter.node.toTypeName()
                    val exportCode = "${graphProperty}.${exporter.reference}()"
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
}

private fun getModuleProperty(module: Module): PropertySpec {
    return PropertySpec.builder(module.id.moduleName(), module.node.toTypeName(), PRIVATE).apply {
        val propertyCode = "${module.source.moduleName()}.${module.reference}().get()"
        delegate(codeBlock = CodeBlock.builder().beginControlFlow("lazy").add(propertyCode).endControlFlow().build())
    }.build()
}

private fun getProviderProperty(provider: Provider): PropertySpec {
    return PropertySpec.builder(provider.id.propertyName(), provider.node.toTypeName(), PRIVATE).apply {
        val dependencies = provider.dependencies.joinToString(separator = ",") { "${it.name} = ${it.id.propertyName()}" }
        val propertyCode = "${provider.source.moduleName()}.${provider.reference}($dependencies).get()"
        when (provider.scope) {
            Scope.FACTORY -> getter(getter = FunSpec.getterBuilder().addStatement("return $propertyCode").build())
            Scope.EAGER -> initializer(format = propertyCode)
            Scope.SINGLE -> delegate(codeBlock = CodeBlock.builder().beginControlFlow("lazy").add(propertyCode).endControlFlow().build())
        }
    }.build()
}

private fun Id.propertyName(): String = "`DEP_${id.sanitize()}`"
private fun Id.moduleName(): String = "MOD_${id.sanitize()}"
private fun String.sanitize(): String {
    return asSequence().joinToString(separator = "") { char ->
        when (char) {
            '_' -> "__"
            '<', '>', '.' -> "_"
            else -> char.toString()
        }
    }
}
