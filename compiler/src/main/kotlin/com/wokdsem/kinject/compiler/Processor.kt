package com.wokdsem.kinject.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

@AutoService(SymbolProcessorProvider::class)
public class KinjectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): KinjectProcessor = KinjectProcessor(environment)
}

public class KinjectProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(GRAPH)
            .filterIsInstance<KSClassDeclaration>()
            .collect { graph -> with(System.currentTimeMillis()) { processGraphDeclaration(graph).map { it to (System.currentTimeMillis() - this) } } }
            .onSuccess { graphs -> reportCompilation(environment, graphs) }
            .fold(
                onSuccess = { graphs -> graphs.forEach { (graph, _) -> generate(graph = graph, codeGenerator = environment.codeGenerator) } },
                onError = { failure -> failure.errorNodes.forEach { environment.logger.error(failure.message, it) } }
            )
        return emptyList()
    }
}
