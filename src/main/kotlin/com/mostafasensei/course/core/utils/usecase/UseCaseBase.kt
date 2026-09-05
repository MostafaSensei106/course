package com.mostafasensei.course.core.utils.usecase

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Result


fun interface UseCaseBase<out T,in Params> {
    suspend operator fun invoke(params: Params): Result<T, Failures>
}

object NoParams

data class PaginationParams (
    val page: Int = 1,
    val perPage: Int = 10
)