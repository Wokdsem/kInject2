package com.wokdsem.kinject2p.exporters

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.asserCompilationError
import org.junit.jupiter.api.Test

class ExportMissingPropertiesTest {

    @Test
    fun `assert that all exporters' properties are provided`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             @Graph class TestGraph {
                fun exportE() = export<E>()
             }
             interface E {
                val number: Int
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Undefined provider for exporter's property")
    }

}