package com.mostafasensei.course.core.utils.result

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.error.StorageErrorHandler


typealias StorageResult<T> = Result<T, Failures>

object Executor {
     inline fun <T> execute(
        crossinline action:  () -> T,
        ): StorageResult<T> = Result.tryCatching(
            onError = { e -> StorageErrorHandler.handle(e) },
            action = { action() }
        )
}