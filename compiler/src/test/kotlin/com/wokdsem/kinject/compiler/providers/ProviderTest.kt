package com.wokdsem.kinject.compiler.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject.compiler.getCompilation
import org.junit.jupiter.api.Test

class ProviderTest {

    @Test
    fun `assert that function types can be provided`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             package test
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.import.import
             import com.wokdsem.kinject.scope.exportSingle
             @Graph class TestGraph {
                fun provideFunction() = exportSingle<(Int) -> String> { { n -> "*".repeat(n) } } 
             }
        """
        )
        val compilation = getCompilation(graph, "TestGraph", `package` = "test")
        runCatching {
            compilation.getKDep("function1_ofInt_andString")
        }.onFailure { error("Missing dependency") }
    }

}