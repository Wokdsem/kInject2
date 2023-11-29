package com.wokdsem.kinject.compiler.graph

import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.wokdsem.kinject.compiler.compile
import org.junit.jupiter.api.Test
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.primaryConstructor

class GraphTest {

    @Test
    fun `assert kinject graph is generated`() {
        val graph = kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             @Graph class TestGraph
        """
        )
        compile(graph).classLoader.run {
            val kTestGraphClass = loadClass("KTestGraph").kotlin
            val testGraph = checkNotNull(value = loadClass("TestGraph").kotlin.primaryConstructor).call()
            val kGraphInstance = with(checkNotNull(kTestGraphClass.companionObject)) { declaredFunctions.first { it.name == "from" }.call(objectInstance, testGraph, null) }
            assert(value = kTestGraphClass.isInstance(value = kGraphInstance))
        }
    }

    @Test
    fun `assert kinject can process more than one graph at once`() {
        val graphs = kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             @Graph class TestGraph
             @Graph class TestGraph2
        """
        )
        compile(graphs).classLoader.run {
            loadClass("KTestGraph").kotlin
            loadClass("KTestGraph2").kotlin
        }
    }

    @Test
    fun `assert default graph name is overwritten`() {
        val graph = kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             @Graph(name = "MyGraph") class TestGraph
        """
        )
        compile(graph).classLoader.run {
            loadClass("MyGraph").kotlin
        }
    }

}