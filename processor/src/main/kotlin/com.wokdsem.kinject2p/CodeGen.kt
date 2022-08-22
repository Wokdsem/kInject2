package com.wokdsem.kinject2p

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

private const val GRAPH_PARAM = "graph"

private val suppress = listOf("PrivatePropertyName", "RemoveRedundantBackticks", "RedundantVisibilityModifier", "PropertyName", "unused")

internal fun generate(graph: Graph, codeGenerator: CodeGenerator) {
    val root = graph.root
    val graphClass = root.toClassName()
    val graphVisibility = checkNotNull(root.getVisibility().toKModifier())
    val kGraphPackage = root.packageName.asString()
    val kGraphName = "K${root.simpleName.getShortName()}"
    val kGraphClass = ClassName(kGraphPackage, kGraphName)
    FileSpec.builder(kGraphPackage, kGraphName).addType(
        TypeSpec.classBuilder(kGraphName).apply {
            addAnnotation(AnnotationSpec.builder(Suppress::class).apply { suppress.onEach { addMember("%S", it) } }.build())
            primaryConstructor(primaryConstructor = FunSpec.constructorBuilder().addParameter(GRAPH_PARAM, graphClass).addModifiers(PRIVATE).build())
            addProperty(propertySpec = PropertySpec.builder(GRAPH_PARAM, graphClass).initializer(GRAPH_PARAM).addModifiers(PRIVATE).build())
            addType(typeSpec = getCompanionBuilder(graphClass, kGraphClass, graphVisibility))
            getExportedProperties(refs = graph.providers.asSequence().filter(Provider::exported).map { Reference(it.id, it.type) }.toList()).forEach(::addProperty)
            graph.providers.forEach { provider -> addProperty(propertySpec = getProperty(provider)) }
        }.build()
    ).build().writeTo(codeGenerator = codeGenerator, aggregating = false, originatingKSFiles = graph.files)
}

private fun getCompanionBuilder(graphClass: TypeName, kGraphClass: TypeName, visibility: KModifier): TypeSpec {
    return TypeSpec.companionObjectBuilder().addFunction(
        FunSpec.builder("from").addModifiers(visibility).addParameter(GRAPH_PARAM, graphClass).returns(kGraphClass).addStatement("return %T($GRAPH_PARAM)", kGraphClass).build()
    ).build()
}

private fun getProperty(provider: Provider): PropertySpec {
    return PropertySpec.builder(provider.id.valName(), provider.type.toTypeName(), PRIVATE).apply {
        val dependencies = provider.dependencies.joinToString(separator = ",") { "${it.name} = ${it.id.valName()}" }
        val propertyCode = "${GRAPH_PARAM}.${provider.providerFunction}($dependencies).get()"
        when (provider.scope) {
            Scope.FACTORY -> getter(getter = FunSpec.getterBuilder().addStatement("return $propertyCode").build())
            Scope.EAGER -> initializer(format = propertyCode)
            Scope.SINGLE -> delegate(codeBlock = CodeBlock.builder().beginControlFlow("lazy").add(propertyCode).endControlFlow().build())
        }
    }.build()
}

private fun getExportedProperties(refs: List<Reference>): List<PropertySpec> {
    data class Property(val id: Id, val type: KSType, val name: String)

    val regex = """^([^<]+)\b(?:<(.+)>)?$""".toRegex()
    fun List<Reference>.name(grade: Int = 1): List<Property> {
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
        return groupBy { ref -> ref.id.id.name().replaceFirstChar(Char::lowercase) }.entries.flatMap { (name, refs) ->
            if (refs.size == 1) listOf(element = refs.first().run { Property(id, type, name) }) else refs.name(grade + 1)
        }
    }
    return refs.name().map { (id, type, name) ->
        PropertySpec.builder(name, type.toTypeName(), PUBLIC).apply {
            getter(getter = FunSpec.getterBuilder().addStatement("return ${id.valName()}").build())
        }.build()
    }
}

private class Reference(val id: Id, val type: KSType)

private fun Id.valName() = "`_${id.replace("_", "__").replace("""[<>.]""".toRegex(), "_")}`"
