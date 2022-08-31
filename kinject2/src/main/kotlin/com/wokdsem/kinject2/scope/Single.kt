package com.wokdsem.kinject2.scope

/**
 * Provides an internal dependency to the graph of dependencies with <singleton> scope. That is, only one instance of this type
 * will be created in the graph. Internal dependencies are not accessible in the generated KGraph.
 *
 * Single type is expected to be returned from a method member inside a graph or module, it won't be processed otherwise.
 * Supplied dependency type is determined by the generic type T. If more than one provider supplies the same dependency, kInject
 * will notify an error stating this situation as non-allowed. kInject is able to deal with type erasure, that way,
 * ClassWithGeneric<T1> will be treated as a different type than ClassWithGeneric<T2>.
 *
 * Typealias is not resolved during the graph compilation, thus a typealias will be considered a different type than its underlying type.
 * This property can be helpful to provide disambiguation in the event that providing multiple instances of the same type is required.
 *
 * If defined in the graph, kInject will supply the dependencies to the method where this single is returned. A compilation error
 * will be thrown if dependencies cannot be satisfied.
 *
 */
@JvmInline
public value class Single<T> internal constructor(private val value: Any?) {
    @Suppress("UNCHECKED_CAST") public fun get(): T = value as T
}

/**
 * DSL single declaration. Sets a single scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 */
public inline fun <T> single(provide: () -> T): Single<T> = single(value = provide())

/**
 * Sets a single scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 *
 * @see single
 */
public fun <T> single(value: T): Single<T> = Single(value = value)

