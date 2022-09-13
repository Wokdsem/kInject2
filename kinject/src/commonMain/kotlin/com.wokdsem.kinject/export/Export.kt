package com.wokdsem.kinject.export

import kotlin.jvm.JvmInline

/**
 * When returned from public functions within a graph, asks kInject to export a subgraph determined by the generic parameter @param T.
 * A compile-time error will be thrown if an export is set in the graph more than once.
 * There is are restrictions to the given name to the function where the export is returned, although prefixing with *export* is recommended.
 *
 * ```
 * @Graph
 * class ExampleGraph {
 *  fun provideService(logger: Logger) = single<MyService> { LocalService(logger) }
 *  fun provideLogger() = single { ConsoleLogger() }
 *  fun exportSystem() = export<System>()
 * }
 *
 * interface System {
 *  val service: MyService
 * }
 * ```
 *
 * An export is considered a valid export if and only if its definition does not break any of the following constraints:
 *  Only interfaces can be exported
 *  The interface is not marked as sealed
 *  All declared properties are public properties whose types are supplied as providers in the graph
 * An export declaration is considered a valid export declaration if and only if its definition does not break any of the following constraints:
 *  The function declaration has no parameters
 *  The function declaration is not an extension receiver
 *  The function declaration is not a suspend function
 *  The function declaration does not set generic types
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
 * @see Export
 */
public fun <T : Any> export(): Export<T> = Export(null)
