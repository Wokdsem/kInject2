package com.wokdsem.kinject2p

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders

internal fun compile(vararg sources: SourceFile): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        inheritClassPath = true
        kspWithCompilation = true
        this.sources = sources.toList()
        symbolProcessorProviders = listOf(KinjectProcessorProvider())
    }.compile()
}

internal fun assertErrorScenario(graph: SourceFile, expectedGraph: String, errorMessage: String) {
    compile(graph).run {
        try {
            classLoader.loadClass(expectedGraph)
            org.junit.jupiter.api.fail("Unexpected successful compilation")
        } catch (e: Throwable) {
            assert(value = errorMessage in messages)
        }
    }
}