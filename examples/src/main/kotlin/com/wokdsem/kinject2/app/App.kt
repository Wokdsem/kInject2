package com.wokdsem.kinject2.app

import com.wokdsem.kinject2.Graph

@Graph
class ApplicationGraph

fun main() {
    KApplicationGraph.from(graph = ApplicationGraph())
}
