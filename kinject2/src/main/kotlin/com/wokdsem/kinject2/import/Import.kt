package com.wokdsem.kinject2.import

/**
 * When returned from public functions within a graph or module, tells the graph to import all provided dependencies defined in the given instance.
 * A module can import other modules. A compile-time error will be thrown if a module is added to the graph more than once or the imported module
 * does not provide any dependency to the graph.
 * There are no restrictions to the given name to the function where the import is returned, although prefixing with *import* is recommended.
 *
 * ```
 * @Graph
 * class ExampleGraph {
 *  fun importModule() = import { Module() }
 * }
 *
 * class Module {
 *  fun provideNumber() = single { 5 }
 *  fun importAnother() = import { AnotherModule() }
 * }
 *
 * class AnotherModule {
 *  fun provideText() = single { "Hello world" }
 * }
 * ```
 *
 *  @see com.wokdsem.kinject2.scope.Factory
 *  @see com.wokdsem.kinject2.scope.Single
 *  @see com.wokdsem.kinject2.scope.Eager
 *
 * A module is considered a valid module if and only if its definition does not break any of the following constraints:
 *  Only a variant of a class can be used to define a module
 *  Modules cannot extend any classes other than Any
 *  Modules have no generic types associated
 * An import module declaration is considered a valid module declaration if and only if its definition does not break any of the following constraints:
 *  The function declaration have no parameters
 *  The function declaration is not an extension receiver
 *  The function declaration is not a suspend function
 *  The function declaration does not set generic types
 *  When using a Typealias, the visibility of the Typealias is no more restrictive than the visibility of the class where the declaration is defined
 *
 */
@JvmInline
public value class Import<T> internal constructor(private val value: Any) {
    @Suppress("UNCHECKED_CAST") public fun get(): T = value as T
}

/**
 * DSL import declaration. Sets a module when used inside a module or graph.
 * kInject will evaluate the generic type T to verify that the module declaration meets all requirements expected for a module declaration.
 * If so, all the provided dependencies will be added to the graph, otherwise a compilation error will be thrown.
 * Be careful if you override the default type inference for the type T, as this may cause kInject not to find all the expected declarations.
 *
 * @see Import
 */
public inline fun <T> import(import: () -> T & Any): Import<T> = import(value = import())

/**
 * Sets a module when used inside a module or graph.
 * kInject will evaluate the generic type T to verify that the module declaration meets all requirements expected for a module declaration.
 * If so, all the provided dependencies will be added to the graph, otherwise a compilation error will be thrown.
 * Be careful if you override the default type inference for the type T, as this may cause kInject not to find all the expected declarations.
 *
 * @see import
 * @see Import
 */
public fun <T> import(value: T & Any): Import<T> = Import(value = value)