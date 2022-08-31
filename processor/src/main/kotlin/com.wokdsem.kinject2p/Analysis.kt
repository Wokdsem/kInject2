package com.wokdsem.kinject2p

import com.google.devtools.ksp.symbol.KSNode
import com.wokdsem.kinject2p.Analysis.Error
import com.wokdsem.kinject2p.Analysis.Failure

internal sealed class Analysis<out T> {
    internal class Success<T>(val result: T) : Analysis<T>()
    internal class Failure(val error: Error) : Analysis<Nothing>()
    internal class Error private constructor(val message: String, val errorNodes: Array<out KSNode>) {
        companion object {
            fun newInstance(message: String, refErrorNode: KSNode, vararg complementaryNodes: KSNode): Error {
                return Error(message, arrayOf(refErrorNode, *complementaryNodes))
            }
        }
    }
}

internal val SUCCESS = Unit.success

internal val <T> T.success: Analysis<T> get() = Analysis.Success(this)
internal fun <T> fail(message: String, errorNode: KSNode, vararg complementaryNodes: KSNode): Analysis<T> {
    return Failure(error = Error.newInstance(message, errorNode, *complementaryNodes))
}

internal inline fun <T, R> Analysis<T>.map(mapper: (T) -> R): Analysis<R> = rawFold(onSuccess = { Analysis.Success(result = mapper(it.result)) }, onError = { it })
internal inline fun <T, R> Analysis<T>.flatMap(mapper: (T) -> Analysis<R>): Analysis<R> = rawFold(onSuccess = { mapper(it.result) }, onError = { it })

internal inline fun <T> Analysis<T>.validate(validator: (T) -> Analysis<Unit>): Analysis<T> = rawFold(onSuccess = { validator(it.result).flatMap { this } }, onError = { this })

internal inline fun <T, R> Analysis<T>.fold(onSuccess: (T) -> R, onError: (Error) -> R): R = rawFold(onSuccess = { onSuccess(it.result) }, onError = { onError(it.error) })

internal inline fun <T> Analysis<T>.getOr(or: (Failure) -> T): T = rawFold(onSuccess = { it.result }, onError = or)

private inline fun <T, R> Analysis<T>.rawFold(onSuccess: (Analysis.Success<T>) -> R, onError: (Failure) -> R): R {
    return when (this) {
        is Failure -> onError(this)
        is Analysis.Success -> onSuccess(this)
    }
}

internal fun <T, R> Sequence<T>.collect(operation: (T) -> Analysis<R>): Analysis<List<R>> = mapTo(mutableListOf()) { operation(it).getOr { failure -> return failure } }.success

internal fun <T> Sequence<T>.onEachUntilError(operation: (T) -> Analysis<*>): Analysis<Unit> {
    for (element in this) {
        val result = operation(element)
        if (result is Failure) return result
    }
    return SUCCESS
}