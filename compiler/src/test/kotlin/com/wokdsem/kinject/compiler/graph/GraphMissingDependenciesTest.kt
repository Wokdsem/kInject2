package com.wokdsem.kinject.compiler.graph

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import org.junit.jupiter.api.Test

class GraphMissingDependenciesTest {

    @Test
    fun `assert that all required dependencies are provided`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.factory
             @Graph class TestGraph {
                fun provideText(times: Int) = factory { "*".repeat(times) }
             }
            """
        )
        asserCompilationError(graph, "KTestGraph", "The provider for dependency times is missing")
    }

}