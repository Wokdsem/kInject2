package com.wokdsem.kinject.compiler.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.getCompilation
import org.junit.jupiter.api.Test

class OverrideTest {

    @Test
    fun `assert provider is overridden`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.exportFactory
             @Graph class TestGraph {
                fun provideNumber() = exportFactory { 0 }
             }

             class ProvidersOverride : KTestGraph.Providers {
                override fun int() = { 5 }
             }   
        """
        )
        val compilation = getCompilation(graph, "TestGraph", override = "ProvidersOverride")
        assert(compilation.getKDep("int") == 5)
    }

}