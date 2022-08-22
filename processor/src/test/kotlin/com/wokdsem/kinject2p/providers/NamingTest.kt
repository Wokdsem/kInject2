package com.wokdsem.kinject2p.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.getCompilation
import org.junit.jupiter.api.Test

class NamingTest {

    @Test
    fun `assert dependency is exported with its type name`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.exportFactory
             @Graph class TestGraph {
                fun provideNumber() = exportFactory { 0 }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph")
        assert(compilation.getKDep("int") == 0)
    }

    @Test
    fun `assert underscores are sanitized by using double-underscore`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.exportFactory
             @Suppress("ClassName")
             class _Dep2
             @Graph class TestGraph {
                fun provideDep2() = exportFactory { _Dep2() }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph")
        runCatching { compilation.getKDep("__Dep2") }.onFailure { error("Missing dependency") }
    }

    @Test
    fun `assert package name is used to resolve a dependencies naming conflict`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             package test
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.exportFactory
             class Int
             @Graph class TestGraph {
                fun provideNumber() = exportFactory { 5 }
                fun provideTestInt() = exportFactory { Int() }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph", `package` = "test")
        runCatching {
            compilation.getKDep("testInt")
            compilation.getKDep("kotlinInt")
        }.onFailure { error("Missing dependency") }
    }

    @Test
    fun `assert names for types with generics are named in a more human readable style`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.exportFactory
             @Suppress("ClassName")
             @Graph class TestGraph {
                fun providePair() = exportFactory { "key" to 5 }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph")
        runCatching { compilation.getKDep("pair_ofString_andInt") }.onFailure { error("Missing dependency") }
    }

}