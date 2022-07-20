package com.wokdsem.kinject2p

import com.tschuchort.compiletesting.*
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.reflect.full.*

class GraphTest {

    @Test
    fun `assert that only classes can be annotated with graph`() {
        val graph = kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             @Graph interface TestGraph
        """
        )
        assertErrorScenario(graph, "KTestGraph", "Only classes can be annotated as Graphs")
    }

    @Test
    fun `assert that the graph does not extend any class`() {
        val graph = kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             open class A 
             @Graph class TestGraph : A()
        """
        )
        assertErrorScenario(graph, "KTestGraph", "A graph cannot extend other classes or implement any interfaces")
    }

    private fun assertErrorScenario(graph: SourceFile, expectedGraph: String, errorMessage: String) {
        compile(graph).run {
            try {
                classLoader.loadClass(expectedGraph)
                fail("Unexpected successful compilation")
            } catch (e: Throwable) {
                assert(value = errorMessage in messages)
            }
        }
    }

    @Test
    fun `assert kinject graph is generated`() {
        val graph = kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             @Graph class TestGraph
        """
        )
        compile(graph).classLoader.run {
            val kTestGraphClass = loadClass("KTestGraph").kotlin
            val testGraph = checkNotNull(value = loadClass("TestGraph").kotlin.primaryConstructor).call()
            val kGraphInstance = with(checkNotNull(kTestGraphClass.companionObject)) { declaredFunctions.first { it.name == "from" }.call(objectInstance, testGraph) }
            assert(value = kTestGraphClass.isInstance(value = kGraphInstance))
        }
    }

    @Test
    fun `assert kinject can process more than one graph at once`() {
        val graphs = kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             @Graph class TestGraph
             @Graph class TestGraph2
        """
        )
        compile(graphs).classLoader.run {
            loadClass("KTestGraph").kotlin
            loadClass("KTestGraph2").kotlin
        }
    }

    private fun compile(vararg sources: SourceFile): KotlinCompilation.Result {
        return KotlinCompilation().apply {
            inheritClassPath = true
            kspWithCompilation = true
            this.sources = sources.toList()
            symbolProcessorProviders = listOf(KinjectProcessorProvider())
        }.compile()
    }

}