package com.wokdsem.kinject2p

import com.google.devtools.ksp.symbol.KSNode
import com.wokdsem.kinject2p.Analysis.Error
import com.wokdsem.kinject2p.Analysis.Failure

internal sealed class Analysis<out T> {
    internal class Success<T>(val result: T) : Analysis<T>()
    internal class Failure(val error: Error) : Analysis<Nothing>()
    internal class Error(val message: String, val root: Array<out KSNode>)
}

internal val <T> T.success: Analysis<T> get() = Analysis.Success(this)
internal fun <T> fail(message: String, vararg root: KSNode): Analysis<T> = Failure(error = Error(message, root))

internal inline fun <T, R> Analysis<T>.map(mapper: (T) -> R): Analysis<R> = rawFold(onSuccess = { Analysis.Success(result = mapper(it.result)) }, onError = { it })
internal inline fun <T, R> Analysis<T>.flatMap(mapper: (T) -> Analysis<R>): Analysis<R> = rawFold(onSuccess = { mapper(it.result) }, onError = { it })

internal inline fun <T> Analysis<T>.getOr(or: (Failure) -> T): T = rawFold(onSuccess = { it.result }, onError = or)

private inline fun <T, R> Analysis<T>.rawFold(onSuccess: (Analysis.Success<T>) -> R, onError: (Failure) -> R): R {
    return when (this) {
        is Failure -> onError(this)
        is Analysis.Success -> onSuccess(this)
    }
}