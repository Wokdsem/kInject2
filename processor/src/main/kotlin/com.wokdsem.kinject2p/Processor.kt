package com.wokdsem.kinject2p

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.*

public class KinjectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): KinjectProcessor = KinjectProcessor(environment)
}

public class KinjectProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        run processor@{
            resolver.getSymbolsWithAnnotation(GRAPH).filterIsInstance<KSClassDeclaration>().mapTo(mutableListOf()) { graph ->
                processGraph(graph).getOr { failure ->
                    failure.error.root.forEach { environment.logger.error(failure.error.message, it) }
                    return@processor
                }
            }.forEach { graph -> generate(graph = graph, codeGenerator = environment.codeGenerator) }
        }
        return emptyList()
    }
}
