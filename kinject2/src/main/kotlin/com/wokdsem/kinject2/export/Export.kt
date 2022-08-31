package com.wokdsem.kinject2.export

/**
 * Asks kInject to export a subgraph with type @param T. The type T must represent a public interface whose
 * internal members are all public properties whose types must match the dependencies previously
 * defined in the graph.
 *
 * The graph compilation will fail if any member other than public properties is found or if kInject is
 * unable to match the property type with any of the declared dependencies.
 *
 */
@Suppress("unused")
@JvmInline
public value class Export<T> internal constructor(@Suppress("unused") private val value: Any?)

/**
 * Sets an exported interface when used inside a graph. Generic type T will define the type
 * is expected to be exported.
 *
 */
public fun <T : Any> export(): Export<T> = Export(null)
