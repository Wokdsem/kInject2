package com.wokdsem.kinject2p.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.assertErrorScenario
import com.wokdsem.kinject2p.compile
import org.junit.jupiter.api.Test

class ProvideDuplicatedTest {

    @Test
    fun `assert that provide same dependency twice is not allowed`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                fun provideString() = factory { "First" }
                fun provideAnotherString() = factory { "Second" }
             }
        """
        )
        assertErrorScenario(graph, "KTestGraph", "Providers clash, a dependency type can only be provided once")
    }

    @Test
    fun `assert that I can provide same dependency type by using a typealias`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.factory
             import com.wokdsem.kinject2.scope.Factory
             private typealias AnotherString = String
             @Graph class TestGraph {
                fun provideString() = factory { "First" }
                fun provideAnotherString():Factory<AnotherString> = factory { "Second" }
             }
        """
        )
        compile(graph).classLoader.loadClass("KTestGraph").kotlin
    }

}