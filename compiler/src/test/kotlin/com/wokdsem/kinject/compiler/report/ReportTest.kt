package com.wokdsem.kinject.compiler.report

import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.wokdsem.kinject.compiler.CompilerConfiguration
import com.wokdsem.kinject.compiler.compile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class ReportTest {

    @Test
    fun `assert compilation report is generated`() {
        val graph = kotlin("TestGraph.kt", getReportGraph())
        val compilation = compile(graph)
        assert(
            "kInject graph compilation report ->\n" +
                    "Graph: TestGraph\n" +
                    "    #Files: 1\n" +
                    "    #Modules: 1\n" +
                    "    #Providers: 3\n" +
                    "    Compilation time:" in compilation.messages
        )
    }

    @Test
    fun `assert graph representation is generated`() {
        val graph = kotlin("TestGraph.kt", getReportGraph())
        val compilation = compile(configuration = CompilerConfiguration(enableGraphGeneration = true), graph)
        assert(
            File(compilation.outputDirectory.parent, "TestGraph.html").readText() == """
        
        <!DOCTYPE html>
        <html>
        <head>
          <title>kI2 TestGraph</title>
          <script src="https://unpkg.com/viz.js@2.1.2/viz.js"></script>
          <script src="https://unpkg.com/viz.js@2.1.2/full.render.js"></script>
        </head>
        <body>
          <i>Scopes shapes</i> (<b>eager</b>=<i>diamond</i> <b>single</b>=<i>oval</i> <b>factory</b>=<i>box</i>)<br>
          <i>Exported provider</i> (<b>exported</b>=<i>bold shape</i>)<br><br>
          <div id="g"></div>
          <script> new Viz().renderSVGElement(`digraph G { fontsize=8;compound=true;concentrate=true;style=dashed; subgraph "cluster_Module"{label="Module";color=blue;margin=25;"Module"[shape=point style=invis];"Int"}"Int" [shape=box style=""];"Int" -> {}"Char" [shape=diamond style=""];"Char" -> {}"String" [shape=oval style=bold];"String" -> {"Int";"Char"} }`).then(function(element) { document.getElementById('g').appendChild(element); }) </script>
        </body>
        </html>
        
        """.trimIndent()
        )
    }

    private fun getReportGraph(): String {
        return """
             package test
             import com.wokdsem.kinject.Graph
             import com.wokdsem.kinject.import.import
             import com.wokdsem.kinject.scope.factory
             import com.wokdsem.kinject.scope.eager
             import com.wokdsem.kinject.scope.exportSingle
             @Graph class TestGraph {
                fun importModule() = import { Module() }
                fun provideChar() = eager { '*' }
                fun provideString(number: Int, character: Char) = exportSingle { "*".repeat(number) }
             }
             class Module {
                fun provideNumber() = factory { 5 }
             } 
        """
    }

}