package com.wokdsem.kinject2p.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.asserCompilationError
import org.junit.jupiter.api.Test

class ProvideSyntaxTest {

    @Test
    fun `assert that private is not a valid visibility for providers`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                private fun provideString() = factory { "DEP" }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Only public or internal visibility modifiers are allowed")
    }

    @Test
    fun `assert that protected is not a valid visibility for providers`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Suppress("ProtectedInFinal","RedundantSuppression")
             @Graph class TestGraph {
                protected fun provideString() = factory { "DEP" }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Only public or internal visibility modifiers are allowed")
    }

    @Test
    fun `assert that suspend is not a valid modifier for providers`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Suppress("RedundantSuspendModifier","RedundantSuppression")
             @Graph class TestGraph {
                suspend fun provideString() = factory { "DEP" }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Suspend functions are not allowed on kInject declarations")
    }

    @Test
    fun `assert that setting generics is not allowed for providers`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                fun <T> provideString() = factory { "DEP" }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "A kInject declaration cannot be parametrized with generic types")
    }

    @Test
    fun `assert that extensions are not allowed for providers`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Suppress("unused","UnusedReceiverParameter","RedundantSuppression")
             @Graph class TestGraph {
                fun Int.provideString() = factory { "DEP" }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Extensions are not allowed on kInject declarations")
    }

    @Test
    fun `assert that default values are not allowed for providers' deps`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                fun provideTimes() = factory { 5 }
                fun provideString(times: Int = 5) = factory { "*".repeat(times) }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Default values are not allowed")
    }

    @Test
    fun `assert that vararg values are not allowed for providers' deps`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                fun provideTimes() = factory { 5 }
                fun provideString(vararg times: Int) = factory { "*".repeat(times.firstOrNull() ?: 0) }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Vararg param is not allowed")
    }

    @Test
    fun `assert that when using typealias as return type this must be public or internal`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             import com.wokdsem.kinject2.scope.Factory
             private typealias Times = Int
             @Graph class TestGraph {
                fun provideTimes(): Factory<Times> = factory { 5 }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Only public or internal visibility modifiers are allowed for typealias")
    }

}