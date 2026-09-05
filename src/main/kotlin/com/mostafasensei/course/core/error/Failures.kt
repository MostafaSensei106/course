package com.mostafasensei.course.core.error

sealed class Failures(
    open val message: String,
    open val code: String? = null,
) {
    data class ServerFailure(
        override val message: String,
        override val code: String? = null,
    ) : Failures(message, code)

    data class CacheFailure(
        override val message: String,
        override val code: String? = null,
    ) : Failures(message, code)

    data class CooldownFailure(
        override val message: String,
        override val code: String? = null,
    ) : Failures(message, code)

    data class NetworkFailure(
        override val message: String,
        override val code: String? = null,
    ) : Failures(message, code)

    data class UnknownFailure(
        override val message: String,
        override val code: String? = null,
    ) : Failures(message, code)

    data class TimeoutFailure(
        override val message: String,
        override val code: String? = null,
    ) : Failures(message, code)

    data class OfflineFailure(
        override val message: String,
        override val code: String? = null,
    ) : Failures(message, code)

    sealed class LocalStorageFailure(
        override val message: String,
        override val code: String? = null,
        open val originalError: Any? = null,
        open val stackTrace: Throwable? = null,
    ) : Failures(message, code) {

        data class DuplicateEntryFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)

        data class NotFoundFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)

        data class ForeignKeyConstraintFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)

        data class NotNullConstraintFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)

        data class DatabaseLockedFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)

        data class DiskFullFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)

        data class DatabaseCorruptedFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)

        data class InvalidDataFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)

        data class UnitAlreadyRentedFailure(
            override val message: String,
            override val code: String? = null,
            override val originalError: Any? = null,
            override val stackTrace: Throwable? = null,
        ) : LocalStorageFailure(message, code, originalError, stackTrace)
    }
}