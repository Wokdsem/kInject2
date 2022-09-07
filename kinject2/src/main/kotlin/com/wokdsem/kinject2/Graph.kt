package com.wokdsem.kinject2

import com.wokdsem.kinject2.export.Export
import com.wokdsem.kinject2.import.Import
import com.wokdsem.kinject2.scope.*

/**
 * The graph annotation is the starting point for defining a dependency graph. A graph is nothing but an aggregation of kInject declarations.
 * There are no constraints in the name you give to your graph.
 *
 * Three are the types of kInject declarations:
 * - The *providers*, to define the graph dependencies.
 * @see Factory
 * @see Single
 * @see Eager
 * - The *imports*, to provide the dependencies defined in a module.
 * @see Import
 * - The *exports*, to publish a subset of dependencies.
 * @see Export
 *
 * Only *public* declarations will be gathered by kInject. Setting declaration whose visibility is other than public is allowed, just beware
 * that kInject will just ignore them.
 *
 * ```
 *  @Graph
 *  class ExampleGraph {
 *    fun provideNumber() = single { 5 }
 *    fun importModule() = import { Module() }
 *    fun export() = export<Export>()
 *  }
 *
 *  class Module {
 *    fun provideText() = single { "Hello world!" }
 *  }
 *
 *  interface Export {
 *    val number: Int
 *    val text: String
 *  }
 * ```
 *
 * A graph is considered a valid graph if and only if does not break any of the following constraints:
 *  Graph visibility must be public or internal
 *  Graphs cannot extend any classes other than Any
 *  Graphs have not generic types associated
 *  All declarations meet the requirements for its declaration type
 *  There is no a dependencies cycle between the provided dependencies
 *
 * If the graph meets all the requirements, an instantiable K<graph> source file will be generated. Otherwise, an error
 * will be thrown in compile-time. The generated K<graph> file will replicate the visibility of the given graph.
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Graph
