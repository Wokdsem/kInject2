package com.wokdsem.kinject.compiler.graph

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import org.junit.jupiter.api.Test

class GraphSyntaxTest {

    @Test
    fun `assert that protected graph visibility is not allowed`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             @Graph protected class TestGraph
        """
        )
        asserCompilationError(graph, "KTestGraph", "Only public or internal visibility modifiers are allowed")
    }

    @Test
    fun `assert that private graph visibility is not allowed`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             @Graph private class TestGraph
        """
        )
        asserCompilationError(graph, "KTestGraph", "Only public or internal visibility modifiers are allowed")
    }

    @Test
    fun `assert that only classes can be annotated with graph`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             @Graph interface TestGraph
        """
        )
        asserCompilationError(graph, "KTestGraph", "Only classes can be annotated as Graphs")
    }

    @Test
    fun `assert that the graph does not extend any class`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             open class A 
             @Graph class TestGraph : A()
        """
        )
        asserCompilationError(graph, "KTestGraph", "Extend is not accepted for this declaration")
    }

    @Test
    fun `assert that graph does not contain any generic type`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             @Graph class TestGraph<T> 
        """
        )
        asserCompilationError(graph, "KTestGraph", "This declaration cannot be parametrized with generic types")
    }

    @Test
    fun `assert that graph name does not contain any invalid char`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             @Graph(name = "It`s not valid") class TestGraph 
        """
        )
        asserCompilationError(graph, "KTestGraph", "Invalid graph name, characters ` and \\ are not allowed")
    }

}