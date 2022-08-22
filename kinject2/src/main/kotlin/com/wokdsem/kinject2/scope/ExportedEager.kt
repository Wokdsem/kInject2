package com.wokdsem.kinject2.scope

/**
 * Provides a dependency to the graph of dependencies with <eager> scope. That is, only one instance of this type
 * will be created in the graph which be instantiated as soon as the graph is loaded. Exported dependencies will be accessible in the generated KGraph.
 *
 * ExportedEager type is expected to be returned from a method member inside a graph or module, it won't be processed otherwise.
 * Supplied dependency type is determined by the generic type T. If more than one provider supplies the same dependency, Kinject
 * will notify an error stating this situation as non-allowed. Kinject is able to deal with type erasure, that way,
 * ClassWithGeneric<T1> will be treated as a different type than ClassWithGeneric<T2>.
 *
 * Typealias is not resolved during the graph compilation, thus a typealias will be considered a different type than its underlying type.
 * This property can be helpful to provide disambiguation in the event that providing multiple instances of the same type is required.
 *
 * If defined in the graph, Kinject will supply the dependencies to the method where this exportedEager is returned. A compilation error
 * will be thrown if dependencies cannot be satisfied.
 *
 */
@JvmInline
public value class ExportedEager<T> internal constructor(private val value: Any?) {
    @Suppress("UNCHECKED_CAST") public fun get(): T = value as T
}

/**
 * DSL exported eager declaration. Sets an exported eager scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 */
public inline fun <T> exportEager(provide: () -> T): ExportedEager<T> = exportEager(value = provide())

/**
 * Sets an exported eager scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 *
 * @see exportEager
 */
public fun <T> exportEager(value: T): ExportedEager<T> = ExportedEager(value = value)

