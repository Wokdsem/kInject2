package com.wokdsem.kinject2p.providers

import com.tschuchort.compiletesting.SourceFile
import com.wokdsem.kinject2p.assertErrorScenario
import com.wokdsem.kinject2p.compile
import org.junit.jupiter.api.Test

class ProvideNullableInstancesTest {

    @Test fun `assert that a provider that supplies a nullable dependency clashes with another provider that supplies a non-nullable instance of that type`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.Factory
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                fun provideString() = factory { "Single String" }
                fun provideNullableString(): Factory<String?> = factory { null }
             }
            """
        )
        assertErrorScenario(graph, "KTestGraph", "Providers clash, a dependency type can only be provided once - typealias may help to break the clash")
    }

    @Test fun `assert that a nullable provider's dependency can be supplied by a provider that provides a non-nullable dependency of that type`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.single
             @Graph class TestGraph {
                fun provideN() = single { 2 }
                fun provideWithNullableStringDep(times: Int?) = single { "*".repeat(times ?: 0) }
             }
            """
        )
        compile(graph).classLoader.loadClass("KTestGraph").kotlin
    }

    @Test fun `assert that a non-nullable provider's dependency cannot be supplied by a provider that provides a nullable dependency of that type`() {
        val graph = SourceFile.kotlin(
            "TestGraph.kt", """
             import com.wokdsem.kinject2.Graph
             import com.wokdsem.kinject2.scope.Factory
             import com.wokdsem.kinject2.scope.factory
             @Graph class TestGraph {
                fun provideNullableN(): Factory<Int?> = factory { null }
                fun provideString(times: Int) = factory { "*".repeat(times) }
             }
            """
        )
        assertErrorScenario(graph, "KTestGraph", "The provider for dependency times requires that times to be nullable")
    }

}