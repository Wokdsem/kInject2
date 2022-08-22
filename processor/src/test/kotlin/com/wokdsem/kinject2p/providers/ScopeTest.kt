package com.wokdsem.kinject2p.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.getCompilation
import org.junit.jupiter.api.Test

class ScopeTest {

    @Test
    fun `assert that a factory provider returns a different instance for each call`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.exportFactory
             class Dep
             @Graph class TestGraph {
                fun provideDep() = exportFactory { Dep() }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph")
        assert(compilation.getKDep("dep") != compilation.getKDep("dep"))
    }

    @Test
    fun `assert that a single provider returns same instance`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.exportSingle
             class Dep
             @Graph class TestGraph {
                fun provideDep() = exportSingle { Dep() }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph")
        assert(compilation.getKDep("dep") == compilation.getKDep("dep"))
    }

    @Test
    fun `assert that an eager dependency returns the same instance`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.exportEager
             class Dep
             @Graph class TestGraph {
                fun provideDep() = exportEager { Dep() }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph")
        assert(compilation.getKDep("dep") == compilation.getKDep("dep"))
    }

    @Test
    fun `assert that an eager dependency is initialized in advance`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.exportEager
             @Graph class TestGraph {
                var initialized = false
                inner class Dep {
                    init {
                        initialized = true
                    }
                }
                fun provideDep() = exportEager { Dep() }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph")
        assert(compilation.getDep("initialized") == true)
    }

}