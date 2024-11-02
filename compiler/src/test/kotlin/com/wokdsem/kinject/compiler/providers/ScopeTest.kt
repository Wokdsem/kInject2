package com.wokdsem.kinject.compiler.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.getCompilation
import org.junit.jupiter.api.Test

class ScopeTest {

    @Test
    fun `assert that a factory provider returns a different instance for each call`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.exportFactory
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
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.exportSingle
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
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.exportEager
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
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.exportEager
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

    @Test
    fun `assert that dependencies of eager dependencies are initialized first`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.exportEager
             import com.wokdsem.kinject.scope.single
             class Dep(val number: Int)
             class Dep2(val number: Int)
             @Graph class TestGraph {
                fun provideDep(number: Int, dep: Dep2) = exportEager { Dep(number) }
                fun provideNumber() = exportEager { 5 }
                fun provideDep2(number: Int) = exportEager { Dep2(number) }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph")
        runCatching {
            compilation.getKDep("dep")
            compilation.getKDep("dep2")
        }.onFailure { error("Wrong initialization order") }
    }

}