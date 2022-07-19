package com.wokdsem.kinject2p

import com.tschuchort.compiletesting.*
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.junit.jupiter.api.Test
import kotlin.reflect.full.*

class GraphTest {

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

    private fun compile(vararg sources: SourceFile): KotlinCompilation.Result {
        return KotlinCompilation().apply {
            inheritClassPath = true
            kspWithCompilation = true
            this.sources = sources.toList()
            symbolProcessorProviders = listOf(KinjectProcessorProvider())
        }.compile()
    }

}