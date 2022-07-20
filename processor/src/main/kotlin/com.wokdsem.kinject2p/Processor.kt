package com.wokdsem.kinject2p

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Visibility.INTERNAL
import com.google.devtools.ksp.symbol.Visibility.PUBLIC
import com.squareup.kotlinpoet.ksp.toTypeName

class KinjectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = KinjectProcessor(environment)
}

class KinjectProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        return resolver.getSymbolsWithAnnotation("com.wokdsem.kinject2.Graph").filter {
            val file = it.containingFile ?: return@filter true
            val graph = it.sanitizeGraphDeclaration(environment.logger)
            val files = mutableListOf(file)
            generate(
                graph = graph, codegen = KspCodegen(environment.codeGenerator, files = files)
            ).let { false }
        }.toList()
    }
}

private fun KSAnnotated.sanitizeGraphDeclaration(logger: KSPLogger): KSClassDeclaration {
    return (this as KSClassDeclaration).also { kClass ->
        fun log(message: String) = logger.error(message, kClass)
        if (kClass.classKind != ClassKind.CLASS) log("Only classes can be annotated as Graphs")
        if (kClass.superTypes.any { it.toTypeName().toString() != "kotlin.Any" }) log("A graph cannot extend other classes or implement any interfaces")
        if (kClass.getVisibility().let { visibility -> visibility != PUBLIC && visibility != INTERNAL }) log("Only public or internal visibility modifiers are allowed")
    }
}