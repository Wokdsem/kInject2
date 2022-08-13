package com.wokdsem.kinject2p.graph

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.assertErrorScenario
import org.junit.jupiter.api.Test

class GraphMissingDependenciesTest {

    @Test
    fun `assert that all required dependencies are provided`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                fun provideText(times: Int) = factory { "*".repeat(times) }
             }
            """
        )
        assertErrorScenario(graph, "KTestGraph", "The provider for dependency times is missing")
    }

}