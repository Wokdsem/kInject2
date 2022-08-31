package com.wokdsem.kinject2p.exporters

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.asserCompilationError
import org.junit.jupiter.api.Test

class ExportDuplicatedTest {

    @Test
    fun `assert that an exported can be declared only once export-export`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
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
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             import com.wokdsem.kinject2.scope.exportSingle
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