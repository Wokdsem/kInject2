package com.wokdsem.kinject.compiler.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.asserCompilationError
import com.wokdsem.kinject.compiler.compile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCompilerApi::class)
class ProvideDuplicatedTest {

    @Test
    fun `assert that provide same dependency twice is not allowed`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.factory
             @Graph class TestGraph {
                fun provideString() = factory { "First" }
                fun provideAnotherString() = factory { "Second" }
             }
        """
        )
        asserCompilationError(graph, "KTestGraph", "Providers clash, a dependency type can only be provided once")
    }

    @Test
    fun `assert that I can provide same dependency type by using a typealias`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.scope.exportFactory
             import com.wokdsem.kinject.scope.ExportedFactory
             typealias AnotherString = String
             @Graph class TestGraph {
                fun provideString() = exportFactory { "First" }
                fun provideAnotherString(): ExportedFactory<AnotherString> = exportFactory { "Second" }
             }
        """
        )
        compile(graph).classLoader.loadClass("KTestGraph").kotlin
    }

}