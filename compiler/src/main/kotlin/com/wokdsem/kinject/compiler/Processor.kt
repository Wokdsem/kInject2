package com.wokdsem.kinject.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

@AutoService(SymbolProcessorProvider::class)
public class KinjectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): KinjectProcessor = KinjectProcessor(environment)
}

public class KinjectProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(GRAPH).filterIsInstance<KSClassDeclaration>().collect { graph -> processGraphDeclaration(graph) }
            .fold(
                onSuccess = { graphs -> graphs.forEach { graph -> generate(graph = graph, codeGenerator = environment.codeGenerator) } },
                onError = { failure -> failure.errorNodes.forEach { environment.logger.error(failure.message, it) } }
            )
        return emptyList()
    }
}
