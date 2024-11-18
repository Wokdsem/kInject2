package com.wokdsem.kinject.compiler.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import org.junit.jupiter.api.Test

class ProvideUnusedTest {

    @Test
    fun `assert that all factory providers are used`(){
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.factory
             @Suppress("RedundantSuspendModifier","RedundantSuppression")
             @Graph class TestGraph {
                fun provideFactory() = factory { "Unused factory" }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Dead/unused provider declaration")
    }

    @Test
    fun `assert that all single providers are used`(){
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.single
             @Suppress("RedundantSuspendModifier","RedundantSuppression")
             @Graph class TestGraph {
                fun provideSingle() = single { "Unused single" }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Dead/unused provider declaration")
    }

}