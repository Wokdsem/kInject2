package com.wokdsem.kinject

import com.wokdsem.kinject.export.Export
import com.wokdsem.kinject.import.Import
import com.wokdsem.kinject.scope.*

/**
 * The graph annotation is the starting point for defining a dependency graph. A graph is nothing but an aggregation of kInject declarations.
 * There are no constraints in the name you give to your graph.
 *
 * Three are the types of kInject declarations:
 * - *Providers*, which are used to define the graph dependencies.
 * [Factory] - [Single] - [Eager]
 * - *Imports*, which allow to add all the dependencies defined in a module to the graph.
 * [Import]
 * - *Exports*, to publish a subset of dependencies.
 * [Export]
 *
 * Only *public* declarations will be gathered by kInject. Setting declaration whose visibility is other than public is allowed, just beware
 * that kInject will just ignore them.
 *
 * ```
 *  @Graph(name = "AwesomeGraph")
 *  class ExampleGraph {
 *    fun provideNumber() = single { 5 }
 *    fun importModule() = import { Module() }
 *    fun export() = export<Export>()
 *  }
 *
 *  class Module {
 *    fun provideText(times: Int) = single { "*".repeat(times) }
 *  }
 *
 *  interface Export {
 *    val number: Int
 *    val text: String
 *  }
 * ```
 *
 * A graph is considered a valid graph if, and only if, it does not break any of the following constraints:
 *  Graph visibility must be public or internal
 *  Graphs cannot extend any classes other than Any
 *  Graphs have no generic types associated
 *  All declarations meet the requirements for its declaration type
 *  There is no a dependencies cycle between the provided dependencies
 *
 * If the graph meets all the requirements, an instantiable kInject graph will be generated, otherwise, an error
 * will be thrown in compile-time.
 * If a non-empty name is set in the graph annotation declaration, this will be used as kInject graph identifier, otherwise,
 * the name of the @Graph annotated class name prefixed by K, K<graph>, will be used by default.
 * The generated kInject graph will replicate the visibility of the underlying graph.
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Graph(
    val name: String = ""
)
