package com.mostafasensei.course.core.utils.result

import com.mostafasensei.course.core.error.Failures
import kotlin.Result



typealias StorageResult = Result<T, Failures>

object Executor {
    suspend inline fun <T> execute(
        crossinline action: suspend () -> T,
        ): StorageResult<T> = Result
}