package com.wokdsem.kinject2p

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

class KinjectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = KinjectProcessor(environment)
}

class KinjectProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        return emptyList()
    }
}