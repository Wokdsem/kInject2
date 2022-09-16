package com.wokdsem.kinject.compiler.exporters

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import org.junit.jupiter.api.Test

class ExportDuplicatedTest {

    @Test
    fun `assert that an exported can be declared only once export-export`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.export.export
             @Graph class TestGraph {
                fun exportE1() = export<E>()
                fun exportE2() = export<E>()
             }
             interface E
        """
        )
        asserCompilationError(graph, "KTestGraph", "Exporters clash, an exporter can only be declared once")
    }

    @Test
    fun `assert that an exported can be declared only once on exportedProvider-export`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.export.export
             import com.wokdsem.kinject.scope.exportSingle
             @Graph class TestGraph {
                fun provideE() = exportSingle<E> { object : E {} }
                fun exportE() = export<E>()
             }
             interface E
        """
        )
        asserCompilationError(graph, "KTestGraph", "Exporters clash, an exporter can only be declared once")
    }

}