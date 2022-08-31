package com.wokdsem.kinject2.scope

/**
 * Provides an internal dependency to the graph of dependencies with <factory> scope. That is, a new instance will be created
 * every time the dependency is required. Internal dependencies are not accessible in the generated KGraph.
 *
 * Factory type is expected to be returned from a method member inside a graph or module, it won't be processed otherwise.
 * Supplied dependency type is determined by the generic type T. If more than one provider supplies the same dependency, kInject
 * will notify an error stating this situation as non-allowed. kInject is able to deal with type erasure, that way,
 * ClassWithGeneric<T1> will be treated as a different type than ClassWithGeneric<T2>.
 *
 * Typealias is not resolved during the graph compilation, thus a typealias will be considered a different type than its underlying type.
 * This property can be helpful to provide disambiguation in the event that providing multiple instances of the same type is required.
 *
 * If defined in the graph, kInject will supply the dependencies to the method where this factory is returned. A compilation error
 * will be thrown if dependencies cannot be satisfied.
 *
 */
@JvmInline
public value class Factory<T> internal constructor(private val value: Any?) {
    @Suppress("UNCHECKED_CAST") public fun get(): T = value as T
}

/**
 * DSL factory declaration. Sets a factory scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 */
public inline fun <T> factory(provide: () -> T): Factory<T> = factory(value = provide())

/**
 * Sets a factory scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 *
 * @see factory
 */
public fun <T> factory(value: T): Factory<T> = Factory(value = value)

