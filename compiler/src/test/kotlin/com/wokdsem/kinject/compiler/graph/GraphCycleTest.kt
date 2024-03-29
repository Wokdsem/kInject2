package com.wokdsem.kinject.compiler.graph

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import org.junit.jupiter.api.Test

class GraphCycleTest {

    @Test
    fun `assert that cycles are detected in compilation time`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.factory
             @Graph class TestGraph {
                fun provideText(times: Int) = factory { "*".repeat(times) }
                fun provideTimes(text: String) = factory { text.length }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Graph cycle detected String -> Int -> String")
    }

}