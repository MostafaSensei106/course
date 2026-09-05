package com.mostafasensei.course.core.utils.result

sealed interface Result<out T, out E> {
    data class Success<T>(val value: T) : Result<T, Nothing>
    data class Failure<E>(val error: E) : Result<Nothing, E>


    companion object {
        inline fun <T, E> tryCatching(onError: (Throwable) -> E, action: () -> T): Result<T, E> {
            return try {
                Success(action())
            } catch (e: Throwable) {
                Failure(onError(e))
            }

        }

        suspend fun <T, E> tryCatchingSuspend(onError: (Throwable) -> E, action: suspend () -> T): Result<T, E> {
            return try {
                Success(action())
            } catch (e: Throwable) {
                Failure(onError(e))
            }
        }


        val <T, E> Result<T, E>.isSuccess: Boolean
            get() = this is Success<T>

        val <T, E> Result<T, E>.isFailure: Boolean
            get() = this is Failure<E>

        val <T, E> Result<T, E>.dataOrNull: T? get() = when (this) {
            is Result.Success -> this.value
            is Result.Failure -> null
        }

        val <T, E> Result<T,E>.errorOrNull: E? get() = when (this) {is Result.Success -> null
        is Result.Failure -> this.error
        }
    }

}