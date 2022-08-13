package com.wokdsem.kinject2p.graph

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.assertErrorScenario
import org.junit.jupiter.api.Test

class GraphSyntaxTest {

    @Test
    fun `assert that protected graph visibility is not allowed`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             @Graph protected class TestGraph
        """
        )
        assertErrorScenario(graph, "KTestGraph", "Only public or internal visibility modifiers are allowed")
    }

    @Test
    fun `assert that private graph visibility is not allowed`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             @Graph private class TestGraph
        """
        )
        assertErrorScenario(graph, "KTestGraph", "Only public or internal visibility modifiers are allowed")
    }

    @Test
    fun `assert that only classes can be annotated with graph`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             @Graph interface TestGraph
        """
        )
        assertErrorScenario(graph, "KTestGraph", "Only classes can be annotated as Graphs")
    }

    @Test
    fun `assert that the graph does not extend any class`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             open class A 
             @Graph class TestGraph : A()
        """
        )
        assertErrorScenario(graph, "KTestGraph", "A graph cannot extend other classes or implement any interfaces")
    }

    @Test
    fun `assert that graph does not contain any generic type`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             @Graph class TestGraph<T> 
        """
        )
        assertErrorScenario(graph, "KTestGraph", "A graph cannot be parametrized with generic types")
    }

}