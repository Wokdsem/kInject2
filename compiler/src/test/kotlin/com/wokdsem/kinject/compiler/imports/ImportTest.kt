package com.wokdsem.kinject.compiler.imports

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import com.wokdsem.kinject.compiler.getCompilation
import org.junit.jupiter.api.Test

class ImportTest {

    @Test
    fun `assert import imports declarations from a module`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             package test
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.import.import
             import com.wokdsem.kinject.scope.exportFactory
             @Graph class TestGraph {
                fun importModule() = import { Module() }
             }
             class Module {
                fun provideNumber() = exportFactory { 5 }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph", `package` = "test")
        runCatching {
            compilation.getKDep("int")
        }.onFailure { error("Missing dependency") }
    }

    @Test
    fun `assert import imports declarations from a second level import declaration`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             package test
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.import.import
             import com.wokdsem.kinject.scope.exportFactory
             @Graph class TestGraph {
                fun importModule() = import { Module() }
             }
             class Module {
                fun importAnother() = import { AnotherModule() }
             }
             class AnotherModule {
                fun provideNumber() = exportFactory { 5 }
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph", `package` = "test")
        runCatching {
            compilation.getKDep("int")
        }.onFailure { error("Missing dependency") }
    }

    @Test
    fun `assert that an import imports at least one dependency`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             package test
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.import.import
             import com.wokdsem.kinject.scope.exportFactory
             @Graph class TestGraph {
                fun importModule() = import { Module() }
             }
             class Module
        """
        )
        asserCompilationError(graph, "KTestGraph", "A module must provide at least one dependency")
    }

}