package com.mostafasensei.course.core.error

sealed class Failures (
    open val code: String?,
    open val message: String,
) {
    data class ServerFailure(override val code: String, override val message: String) : Failures(code, message)
    data class CooldownFailure(override val code: String, override val message: String) : Failures(code, message)
    data class NetworkFailure(override val code: String, override val message: String) : Failures(code, message)
    data class UnknownFailure(override val code: String, override val message: String) : Failures(code, message)
    data class TimeoutFailure(override val code: String, override val message: String) : Failures(code, message)
    data class OtherFailure(override val code: String, override val message: String) : Failures(code, message)
    data class PostgresSqlFailure(
        override val message: String,
        override val code: String? = null,
        val originalError: Any? = null,
        val stackTrace: Throwable? = null,
    ) : Failures(code, message)
}