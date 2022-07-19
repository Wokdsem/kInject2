package com.wokdsem.kinject2

/**
 * The graph annotation is the starting point for defining a dependency graph.
 * If the annotated class meets all the requirements, an instantiable K<graph> source file will be generated.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Graph