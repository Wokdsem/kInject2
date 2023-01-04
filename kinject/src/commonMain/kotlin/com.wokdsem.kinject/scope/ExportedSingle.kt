package com.wokdsem.kinject.scope

import kotlin.jvm.JvmInline

/**
 * Provides a dependency to the graph of dependencies with <singleton> scope. That is, only one instance of this type
 * will be created in the graph. Exported dependencies will be accessible in the generated KGraph.
 *
 * ExportedSingle type is expected to be returned from a method member inside a graph or module, it won't be processed otherwise.
 * Supplied dependency type is determined by the generic type T. If more than one provider supplies the same dependency, kInject
 * will notify an error stating this situation as non-allowed. kInject is able to deal with type erasure, that way,
 * ClassWithGeneric<T1> will be treated as a different type than ClassWithGeneric<T2>.
 *
 * Typealias is not resolved during the graph compilation, thus a typealias will be considered a different type than its underlying type.
 * This property can be helpful to provide disambiguation in the event that providing multiple instances of the same type is required.
 *
 * If defined in the graph, kInject will supply the dependencies to the method where this exportedSingle is returned. A compilation error
 * will be thrown if dependencies cannot be satisfied.
 *
 *  An exportedSingle declaration is considered valid if and only if its definition does not break any of the following constraints:
 *  - The function parameters don't set a default value
 *  - The function parameters are not Vararg
 *  - The function declaration is not an extension receiver
 *  - The function declaration is not a suspend function
 *  - The function declaration does not set generic types
 *  - When using a Typealias, the visibility of the Typealias is no more restrictive than the visibility of the class where the declaration is defined
 *
 */
@JvmInline
public value class ExportedSingle<T> internal constructor(private val value: Any?) {
    @Suppress("UNCHECKED_CAST") public fun get(): T = value as T
}

/**
 * DSL exported single declaration. Sets a single scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 *
 * @see ExportedSingle
 */
public inline fun <T> exportSingle(provide: () -> T): ExportedSingle<T> = exportSingle(value = provide())

/**
 * Sets an exported single scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 *
 * @see exportSingle
 * @see ExportedSingle
 */
public fun <T> exportSingle(value: T): ExportedSingle<T> = ExportedSingle(value = value)

