package com.mostafasensei.course.core.delivery

import com.mostafasensei.course.core.error.Failures
import org.springframework.http.HttpStatus

fun Failures.toHttpStatus(): HttpStatus = when (this) {
    is Failures.LocalStorageFailure.DuplicateEntryFailure -> HttpStatus.CONFLICT
    is Failures.LocalStorageFailure.NotFoundFailure -> HttpStatus.NOT_FOUND
    is Failures.LocalStorageFailure.InvalidDataFailure -> HttpStatus.BAD_REQUEST
    is Failures.LocalStorageFailure.ForeignKeyConstraintFailure -> HttpStatus.CONFLICT
    is Failures.LocalStorageFailure.NotNullConstraintFailure -> HttpStatus.BAD_REQUEST
    else -> HttpStatus.INTERNAL_SERVER_ERROR
}

fun Failures.toCode(fallback: String): String = this.code ?: fallback
