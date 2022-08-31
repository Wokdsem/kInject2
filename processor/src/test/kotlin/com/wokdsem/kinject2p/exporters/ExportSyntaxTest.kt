package com.wokdsem.kinject2p.exporters

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.asserCompilationError
import org.junit.jupiter.api.Test

class ExportSyntaxTest {

    @Test
    fun `assert that only interfaces can be exported`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             @Graph class TestGraph {
                fun exportE() = export<E>()
             }
             class E
        """
        )
        asserCompilationError(graph, "KTestGraph", "Only interfaces can be exported")
    }

    @Test
    fun `assert that sealed interfaces can be exported`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             @Graph class TestGraph {
                fun exportE() = export<E>()
             }
             sealed interface E
        """
        )
        asserCompilationError(graph, "KTestGraph", "Sealed modifier is not allowed")
    }

    @Test
    fun `assert that only immutable properties are valid for exported types`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             import com.wokdsem.kinject2.scope.single
             @Graph class TestGraph {
                fun providerNumber() = single { 5 }
                fun exportE() = export<E>()
             }
             interface E {
                var number: Int
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Only immutable properties are allowed for an exported type")
    }

    @Test
    fun `assert that an exported type cannot contain generic types`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             @Graph class TestGraph {
                fun exportE() = export<E<Int>>()
             }
             interface E<T>
        """
        )
        asserCompilationError(graph, "KTestGraph", "This declaration cannot be parametrized with generic types")
    }

    @Test
    fun `assert that an exported declaration cannot contain any parameter`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             @Graph class TestGraph {
                fun exportE(n: Int) = export<E>()
             }
             interface E
        """
        )
        asserCompilationError(graph, "KTestGraph", "Export declaration does not accept parameters")
    }

    @Test
    fun `assert that private properties cannot be exported`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             import com.wokdsem.kinject2.scope.single
             @Graph class TestGraph {
                fun provideNumber() = single { 5 }
                fun exportE(n: Int) = export<E>()
             }
             interface E {
                private abstract val number: Int
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Export declaration does not accept parameters")
    }

    @Test
    fun `assert that extension properties are not allowed for exporters' properties`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.export.export
             import com.wokdsem.kinject2.scope.single
             @Graph class TestGraph {
                fun provideNumber() = single { 5 }
                fun exportE() = export<E>()
             }
             interface E {
                val Int.number: Int
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Extension is not allowed for exported property")
    }

}