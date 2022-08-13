package com.wokdsem.kinject2p

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.writeTo

private const val GRAPH_PARAM = "graph"

internal class KspCodegen(val generator: CodeGenerator, val files: List<KSFile>)

internal fun generate(root: KSClassDeclaration, codegen: KspCodegen) {
    val graphClass = root.toClassName()
    val graphVisibility = checkNotNull(root.getVisibility().toKModifier())
    val kGraphPackage = root.packageName.asString()
    val kGraphName = "K${root.simpleName.getShortName()}"
    val kGraphClass = ClassName(kGraphPackage, kGraphName)
    FileSpec
        .builder(kGraphPackage, kGraphName)
        .addType(
            TypeSpec.classBuilder(kGraphName)
                .primaryConstructor(primaryConstructor = FunSpec.constructorBuilder().addParameter(GRAPH_PARAM, graphClass).addModifiers(PRIVATE).build())
                .addProperty(propertySpec = PropertySpec.builder(GRAPH_PARAM, graphClass).initializer(GRAPH_PARAM).addModifiers(PRIVATE).build())
                .addType(typeSpec = getCompanionBuilder(graphClass, kGraphClass, graphVisibility)).build()
        ).build().writeTo(codeGenerator = codegen.generator, aggregating = false, originatingKSFiles = codegen.files)
}

private fun getCompanionBuilder(graphClass: TypeName, kGraphClass: TypeName, visibility: KModifier): TypeSpec {
    val from = FunSpec.builder("from").addModifiers(visibility).addParameter(GRAPH_PARAM, graphClass)
        .returns(kGraphClass).addStatement("return %T($GRAPH_PARAM)", kGraphClass).build()
    return TypeSpec.companionObjectBuilder().addFunction(from).build()
}