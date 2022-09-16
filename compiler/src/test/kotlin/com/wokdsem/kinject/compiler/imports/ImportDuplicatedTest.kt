package com.wokdsem.kinject.compiler.imports

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import org.junit.jupiter.api.Test

class ImportDuplicatedTest {

    @Test
    fun `assert that a module can be imported only once`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             package test
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.import.import
             import com.wokdsem.kinject.scope.exportFactory
             import com.wokdsem.kinject.scope.single
             @Graph class TestGraph {
                fun importModule() = import { Module() }
                fun importDuplicatedModule() = import { Module() }
             }
             class Module {
                fun provideNumber() = single { 5 }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Modules clash, a module can be imported only once")
    }

}