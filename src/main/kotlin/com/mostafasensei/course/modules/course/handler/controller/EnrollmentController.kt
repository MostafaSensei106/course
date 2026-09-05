package com.mostafasensei.course.modules.course.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.delivery.toCode
import com.mostafasensei.course.core.delivery.toHttpStatus
import com.mostafasensei.course.core.router.ApiRoutes
import com.mostafasensei.course.core.security.CurrentUser
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.modules.course.handler.dto.*
import com.mostafasensei.course.modules.course.handler.usecase.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping(ApiRoutes.Enrollments.BASE)
class EnrollmentController(
    private val enrollUseCase: EnrollUseCase,
    private val myEnrollmentsUseCase: MyEnrollmentsUseCase,
    private val progressUseCase: UpdateProgressUseCase,
    private val cancelUseCase: CancelEnrollmentUseCase
) {
    @PostMapping
    suspend fun enroll(@Valid @RequestBody req: EnrollRequest): ResponseEntity<ApiResponse<EnrollmentResponse>> =
        enrollUseCase(CurrentUser.requireId() to req.courseId).fold(
            onSuccess = { Responser.created(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("ENROLL_FAILED"), f.message) }
        )

    @GetMapping(ApiRoutes.Enrollments.ME)
    suspend fun my(): ResponseEntity<ApiResponse<List<EnrollmentResponse>>> =
        myEnrollmentsUseCase(CurrentUser.requireId()).fold(
            onSuccess = { Responser.ok(it.map { e -> e.toResponse() }) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_ENROLLMENTS_FAILED"), f.message) }
        )

    @PatchMapping(ApiRoutes.Enrollments.PROGRESS)
    suspend fun updateProgress(@PathVariable id: UUID, @Valid @RequestBody req: UpdateProgressRequest): ResponseEntity<ApiResponse<EnrollmentResponse>> =
        progressUseCase(id to req.progressPercent).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_PROGRESS_FAILED"), f.message) }
        )

    @DeleteMapping(ApiRoutes.Enrollments.BY_ID)
    suspend fun cancel(@PathVariable id: UUID): ResponseEntity<ApiResponse<EnrollmentResponse>> =
        cancelUseCase(id).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("CANCEL_ENROLLMENT_FAILED"), f.message) }
        )
}
