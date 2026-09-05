@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package com.mostafasensei.course.core.utils.result


import kotlinx.coroutines.CancellationException
import java.io.Serializable

@JvmInline
value class Result<out T, out E> @PublishedApi internal constructor(
    @PublishedApi internal val rawValue: Any?
) : Serializable {

    val isSuccess: Boolean
        get() = rawValue !is FailureContainer<*>

    val isFailure: Boolean
        get() = rawValue is FailureContainer<*>

    inline fun getOrNull(): T? = when {
        isFailure -> null
        else -> rawValue as T
    }

    inline fun errorOrNull(): E? = when (rawValue) {
        is FailureContainer<*> -> rawValue.error as E
        else -> null
    }

    @PublishedApi
    internal class FailureContainer<out E>(
        val error: E
    ) : Serializable {
        override fun equals(other: Any?): Boolean =
            other is FailureContainer<*> && error == other.error

        override fun hashCode(): Int = error.hashCode()
        override fun toString(): String = "Failure($error)"
    }

    companion object {
        inline fun <T> success(data: T): Result<T, Nothing> =
            Result(data)

        inline fun <E> failure(error: E): Result<Nothing, E> =
            Result(FailureContainer(error))


        inline fun <T, E> tryCatching(
            onError: (Throwable) -> E,
            action: () -> T
        ): Result<T, E> = try {
            success(action())
        } catch (e: Throwable) {
            failure(onError(e))
        }


        suspend inline fun <T, E> tryCatchingSuspend(
            onError: (Throwable) -> E,
            action: () -> T
        ): Result<T, E> = try {
            success(action())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            failure(onError(e))
        }
    }
}


inline fun <T, E, R> Result<T, E>.fold(
    onSuccess: (data: T) -> R,
    onFailure: (error: E) -> R,
): R = when (val raw = rawValue) {
    is Result.FailureContainer<*> -> onFailure(raw.error as E)
    else -> onSuccess(raw as T)
}

inline fun <T, E> Result<T, E>.getOrElse(fallback: (error: E) -> T): T =
    when (val raw = rawValue) {
        is Result.FailureContainer<*> -> fallback(raw.error as E)
        else -> raw as T
    }

inline fun <T, E> Result<T, E>.onSuccess(action: (data: T) -> Unit): Result<T, E> {
    if (isSuccess) action(rawValue as T)
    return this
}

inline fun <T, E> Result<T, E>.onFailure(action: (error: E) -> Unit): Result<T, E> {
    if (rawValue is Result.FailureContainer<*>) {
        action(rawValue.error as E)
    }
    return this
}

inline fun <T, E, NewData> Result<T, E>.map(
    transform: (data: T) -> NewData
): Result<NewData, E> = when (val raw = rawValue) {
    is Result.FailureContainer<*> -> Result(raw)
    else -> Result.success(transform(raw as T))
}

inline fun <T, E, NewError> Result<T, E>.mapError(
    transform: (error: E) -> NewError
): Result<T, NewError> = when (val raw = rawValue) {
    is Result.FailureContainer<*> -> Result.failure(transform(raw.error as E))
    else -> Result(raw)
}