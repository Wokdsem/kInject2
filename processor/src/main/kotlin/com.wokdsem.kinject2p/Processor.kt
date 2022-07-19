package com.wokdsem.kinject2p

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class KinjectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = KinjectProcessor(environment)
}

class KinjectProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        return resolver.getSymbolsWithAnnotation("com.wokdsem.kinject2.Graph").filter {
            val file = it.containingFile ?: return@filter true
            val graph = it as KSClassDeclaration
            val files = mutableListOf(file)
            generate(
                graph = graph, codegen = KspCodegen(environment.codeGenerator, files = files)
            ).let { false }
        }.toList()
    }
}