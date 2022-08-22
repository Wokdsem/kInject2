package com.wokdsem.kinject2

/**
 * The graph annotation is the starting point for defining a dependency graph.
 * If the annotated class meets all the requirements, an instantiable K<graph> source file will be generated.
 *
 * Only public or internal classes that don't extend any class or implement any interface and are not parametrized
 * with any generic type can be annotated with graph.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Graph
