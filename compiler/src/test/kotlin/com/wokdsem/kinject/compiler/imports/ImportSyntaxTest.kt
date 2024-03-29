package com.wokdsem.kinject.compiler.imports

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import org.junit.jupiter.api.Test

class ImportSyntaxTest {

    @Test
    fun `assert import imports declarations from a module`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             package test
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.import.import
             import com.wokdsem.kinject.scope.exportFactory
             @Graph class TestGraph {
                fun importModule(n: Int) = import { Module() }
             }
             class Module {
                fun provideNumber() = exportFactory { 5 }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Import declaration does not accept parameters")
    }

    @Test
    fun `assert import when importing a module behind a typealias the typealias visibility cannot be more restricted`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             package test
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.import.import
             import com.wokdsem.kinject.import.Import
             import com.wokdsem.kinject.scope.exportFactory
             internal typealias Alias = Module
             @Graph class TestGraph {
                fun importModule(): Import<Alias> = import { Module() }
             }
             class Module {
                fun provideNumber() = exportFactory { 5 }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Typealias visibility must not be more restrictive than the class where it's used")
    }

}