package com.wokdsem.kinject2.scope

/**
 * Provides an internal dependency to the graph of dependencies with <eager> scope. That is, only one instance of this type
 * will be created in the graph which be instantiated as soon as the graph is loaded. Internal dependencies are not accessible in the generated KGraph.
 *
 * Eager type is expected to be returned from a method member inside a graph or module, it won't be processed otherwise.
 * Supplied dependency type is determined by the generic type T. If more than one provider supplies the same dependency, kInject
 * will notify an error stating this situation as non-allowed. kInject is able to deal with type erasure, that way,
 * ClassWithGeneric<T1> will be treated as a different type than ClassWithGeneric<T2>.
 *
 * Typealias is not resolved during the graph compilation, thus a typealias will be considered a different type than its underlying type.
 * This property can be helpful to provide disambiguation in the event that providing multiple instances of the same type is required.
 *
 * If defined in the graph, kInject will supply the dependencies to the method where this eager is returned. A compilation error
 * will be thrown if dependencies cannot be satisfied.
 *
 *  An eager declaration is considered valid if and only if its definition does not break any of the following constraints:
 *  The function parameters don't set a default value
 *  The function parameters are not Vararg
 *  The function declaration is not an extension receiver
 *  The function declaration is not a suspend function
 *  The function declaration does not set generic types
 *  When using a Typealias, the visibility of the Typealias is no more restrictive than the visibility of the class where the declaration is defined
 *
 *
 */
@JvmInline
public value class Eager<T> internal constructor(private val value: Any?) {
    @Suppress("UNCHECKED_CAST") public fun get(): T = value as T
}

/**
 * DSL eager declaration. Sets an eager scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 *
 * @see Eager
 */
public inline fun <T> eager(provide: () -> T): Eager<T> = eager(value = provide())

/**
 * Sets an eager scoped dependency when used inside a module or graph.
 * Set the type T to override the default Kotlin type inference.
 *
 * @see eager
 * @see Eager
 */
public fun <T> eager(value: T): Eager<T> = Eager(value = value)

