package com.mostafasensei.course.core.error

import com.mostafasensei.course.core.error.Failures
import org.hibernate.exception.ConstraintViolationException
import org.postgresql.util.PSQLException
import org.springframework.dao.*
import org.springframework.transaction.CannotCreateTransactionException
import java.io.IOException

object StorageErrorHandler {


    fun isStorageError(throwable: Throwable): Boolean {
        if (throwable is DataAccessException) return true
        if (throwable is PSQLException) return true
        if (throwable is IOException) return true

        val str = throwable.message?.lowercase().orEmpty()
        return str.contains("postgres") ||
                str.contains("psql") ||
                str.contains("constraint") ||
                str.contains("duplicate") ||
                str.contains("not found")
    }


    fun handle(
        error: Throwable,
        isDebug: Boolean = false,
    ): Failures.LocalStorageFailure {
        val root = unwrapException(error)
        val lowerMessage = root.message?.lowercase().orEmpty()

        // 1. Business Logic State Errors
        if (root is IllegalStateException && root.message == "unitAlreadyRented" ||
            lowerMessage.contains("unitalreadyrented")
        ) {
            return Failures.LocalStorageFailure.UnitAlreadyRentedFailure(
                message = "This unit is already rented under an active contract.",
                code = "UNIT_ALREADY_RENTED",
                originalError = error,
                stackTrace = root
            )
        }

        // 2. Not Found / Empty Result
        if (root is EmptyResultDataAccessException ||
            lowerMessage.contains("not found") ||
            lowerMessage.contains("entitynotfoundexception") ||
            lowerMessage.contains("no entity found")
        ) {
            return Failures.LocalStorageFailure.NotFoundFailure(
                message = "The requested record was not found in the database.",
                code = "NOT_FOUND",
                originalError = error,
                stackTrace = root
            )
        }

        // 3. PostgreSQL & Hibernate Constraint Violations
        val psqlException = findPsqlException(error)
        if (psqlException != null) {
            return handlePostgreSqlException(psqlException, root)
        }

        // 4. Spring DataAccess Exceptions (Concurrency & Locks)
        if (error is CannotAcquireLockException ||
            error is PessimisticLockingFailureException ||
            lowerMessage.contains("deadlock") ||
            lowerMessage.contains("lock wait timeout")
        ) {
            return Failures.LocalStorageFailure.DatabaseLockedFailure(
                message = "The record or database is currently locked by another transaction. Please try again.",
                code = "POSTGRES_LOCK_ACQUISITION_FAILED",
                originalError = error,
                stackTrace = root
            )
        }

        // 5. Connection / Connection Pool Failures (HikariCP / Network to DB)
        if (error is CannotCreateTransactionException ||
            error is DataAccessResourceFailureException ||
            lowerMessage.contains("connection refused") ||
            lowerMessage.contains("hikaricp")
        ) {
            return Failures.LocalStorageFailure.DatabaseCorruptedFailure(
                message = "Unable to connect to the PostgreSQL database server.",
                code = "DATABASE_CONNECTION_ERROR",
                originalError = error,
                stackTrace = root
            )
        }

        // 6. Generic Data Integrity / Type Mismatch
        if (error is DataIntegrityViolationException || error is IllegalArgumentException) {
            return Failures.LocalStorageFailure.InvalidDataFailure(
                message = "Invalid data values or data integrity violation.",
                code = "DATA_INTEGRITY_VIOLATION",
                originalError = error,
                stackTrace = root
            )
        }

        // 7. General Fallback
        return Failures.LocalStorageFailure.DatabaseCorruptedFailure(
            message = if (isDebug) root.message ?: "Database error" else "An unexpected database error occurred.",
            code = "DATABASE_UNKNOWN",
            originalError = error,
            stackTrace = root
        )
    }


    private fun handlePostgreSqlException(
        psql: PSQLException,
        root: Throwable
    ): Failures.LocalStorageFailure {
        return when (psql.sqlState) {
            // 23505: unique_violation
            "23505" -> Failures.LocalStorageFailure.DuplicateEntryFailure(
                message = "This record already exists or contains duplicate data.",
                code = "PG_UNIQUE_VIOLATION_23505",
                originalError = psql,
                stackTrace = root
            )

            // 23503: foreign_key_violation
            "23503" -> Failures.LocalStorageFailure.ForeignKeyConstraintFailure(
                message = "This operation cannot be completed because this record is linked to other data.",
                code = "PG_FOREIGN_KEY_VIOLATION_23503",
                originalError = psql,
                stackTrace = root
            )

            // 23502: not_null_violation
            "23502" -> Failures.LocalStorageFailure.NotNullConstraintFailure(
                message = "Required information is missing. Please fill in all required fields.",
                code = "PG_NOT_NULL_VIOLATION_23502",
                originalError = psql,
                stackTrace = root
            )

            // 23514: check_violation
            "23514" -> Failures.LocalStorageFailure.InvalidDataFailure(
                message = "Data failed validation constraints in the database.",
                code = "PG_CHECK_VIOLATION_23514",
                originalError = psql,
                stackTrace = root
            )

            // 53100: disk_full
            "53100" -> Failures.LocalStorageFailure.DiskFullFailure(
                message = "Server disk is full.",
                code = "PG_DISK_FULL_53100",
                originalError = psql,
                stackTrace = root
            )

            // 40P01: deadlock_detected | 55P03: lock_not_available
            "40P01", "55P03" -> Failures.LocalStorageFailure.DatabaseLockedFailure(
                message = "Transaction was aborted due to a database lock or deadlock.",
                code = "PG_LOCK_ERROR_${psql.sqlState}",
                originalError = psql,
                stackTrace = root
            )

            else -> Failures.LocalStorageFailure.InvalidDataFailure(
                message = psql.message ?: "Database constraint violation.",
                code = "PG_ERROR_${psql.sqlState ?: "UNKNOWN"}",
                originalError = psql,
                stackTrace = root
            )
        }
    }

    private fun findPsqlException(throwable: Throwable): PSQLException? {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is PSQLException) return current
            if (current is ConstraintViolationException && current.sqlException is PSQLException) {
                return current.sqlException as PSQLException
            }
            if (current.cause === current) break
            current = current.cause
        }
        return null
    }

    private fun unwrapException(throwable: Throwable): Throwable {
        var root = throwable
        while (root.cause != null && root.cause !== root) {
            root = root.cause!!
        }
        return root
    }
}