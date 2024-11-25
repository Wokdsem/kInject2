package com.wokdsem.kinject.compiler

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.io.File

private const val GRAPH_FILE_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
  <title>kI2 %s</title>
  <script src="https://unpkg.com/viz.js@2.1.2/viz.js"></script>
  <script src="https://unpkg.com/viz.js@2.1.2/full.render.js"></script>
</head>
<body>
  <i>Scopes shapes</i> (<b>eager</b>=<i>diamond</i> <b>single</b>=<i>oval</i> <b>factory</b>=<i>box</i>)<br>
  <i>Exported provider</i> (<b>exported</b>=<i>bold shape</i>)<br><br>
  <div id="g"></div>
  <script> new Viz().renderSVGElement(`digraph G { fontsize=8;compound=true;concentrate=true;style=dashed; %s }`).then(function(element) { document.getElementById('g').appendChild(element); }) </script>
</body>
</html>
"""

internal fun reportCompilation(environment: SymbolProcessorEnvironment, graphs: List<Pair<Graph, Long>>) {
    val graphDir = environment.options["kInject-graphDir"]?.let(::File)?.takeIf { it.exists() || it.mkdirs() }
    graphs.forEach { (graph, compilationTime) ->
        val graphName = graph.name.takeIf(String::isNotEmpty) ?: graph.root.simpleName.getShortName()
        if (graphDir != null) File(graphDir, "$graphName.html").apply { writeText(GRAPH_FILE_TEMPLATE.format(graphName, getDotGraph(graph = graph))) }
        environment.logger.info(
            message = """
                kInject graph compilation report ->
                Graph: $graphName
                    #Files: ${graph.files.size}
                    #Modules: ${graph.modules.size}
                    #Providers: ${graph.providers.size}
                    Compilation time: ${compilationTime.formatCompilationTime()}
                ${if (graphDir != null) "Graph representation: file:///$graphDir/$graphName.html" else ""}
            """.trimIndent(),
            symbol = graph.root
        )
    }
}

private fun getDotGraph(graph: Graph): String {
    return buildString {
        val modulesNames = graph.modules.map(Module::id).name()
        val providersNames = graph.providers.keys.name()
        graph.modules.forEach { module ->
            val id = modulesNames[module.id]
            append("subgraph \"cluster_$id\"{label=\"$id\";color=blue;margin=25;")
            append("\"$id\"[shape=point style=invis];")
            append(module.providers.joinToString(separator = ";") { "\"${providersNames.getValue(it)}\"" })
            append("}")
            append(module.imports.joinToString(separator = ";") {
                val moduleName = modulesNames.getValue(it)
                "\"$id\"->\"$moduleName\"[arrowsize=0.5 arrowhead=vee style=dashed ltail=\"cluster_$id\" lhead=\"cluster_$moduleName\"]"
            })
        }
        graph.providers.values.forEach { provider ->
            val id = providersNames[provider.id]
            val style = if (provider.exported) "bold" else "\"\""
            val shape = when (provider.scope) {
                Scope.EAGER -> "diamond"
                Scope.SINGLE -> "oval"
                Scope.FACTORY -> "box"
            }
            append("\"$id\" [shape=$shape style=$style];")
            append("\"$id\" -> ${provider.dependencies.joinToString(separator = ";", prefix = "{", postfix = "}") { "\"${providersNames.getValue(it.id)}\"" }}")
        }
    }
}

private fun Iterable<Id>.name(): Map<Id, String> {
    fun Iterable<Id>.nameByGrade(grade: Int = 1): List<Pair<Id, String>> {
        fun String.onGrade(): String {
            var count = grade
            return takeLastWhile { it != '.' || --count > 0 }
        }
        return groupBy { id -> id.id.onGrade() }.entries.flatMap { (name, ids) ->
            if (ids.size == 1) listOf(element = ids.first() to name) else ids.nameByGrade(grade + 1)
        }
    }
    return nameByGrade().toMap()
}

private fun Long.formatCompilationTime(): String {
    val hours = if (this > 3_600_000) "${this / 3_600_000}h " else ""
    val minutes = if (this > 60_000) "${this / 60_000 % 60}m " else ""
    val seconds = if (this > 1000) "${this / 1000 % 60}s " else ""
    val milliseconds = "${this % 1000}ms"
    return "$hours$minutes$seconds$milliseconds"
}
