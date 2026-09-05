package com.mostafasensei.course.core.utils.result

sealed interface Result<out T, out E> {
    data class Success<T>(val data: T) : Result<T, Nothing>
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
            is Success -> data
            is Failure -> null
        }

        val <T, E> Result<T,E>.errorOrNull: E? get() = when (this) {is Success -> null
        is Failure -> error
        }

        inline fun <T,E,R> Result<T,E>.fold(
            onSuccess: (data: T) -> R,
            onFailure: (error: E) -> R
        ): R = when (this) {
            is Success -> onSuccess(data)
            is Failure -> onFailure(error)
        }

        inline fun <T, E> Result<T, E>.onFailure(action: (error: E) -> Unit): Result<T, E> {
            if (this is Failure) action(error)
            return this
        }


        inline fun <T, E, NewError> Result<T, E>.mapError(mapper: (error: E) -> NewError): Result<T, NewError> =
            when (this) {
                is Success -> Success(data)
                is Failure -> Failure(mapper(error))
            }

        inline fun <T, E, NewData> Result<T, E>.map(mapper: (data: T) -> NewData): Result<NewData, E> =
            when (this) {
                is Success -> Success(mapper(data))
                is Failure -> Failure(error)
            }


    }

}