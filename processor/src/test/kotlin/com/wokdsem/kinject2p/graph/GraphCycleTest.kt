package com.wokdsem.kinject2p.graph

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.assertErrorScenario
import org.junit.jupiter.api.Test

class GraphCycleTest {

    @Test
    fun `assert that cycles are detected in compilation time`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                fun provideText(times: Int) = factory { "*".repeat(times) }
                fun provideTimes(text: String) = factory { text.length }
             }
        """
        )
        assertErrorScenario(graph, "KTestGraph", "Graph cycle detected String -> Int -> String")
    }

}