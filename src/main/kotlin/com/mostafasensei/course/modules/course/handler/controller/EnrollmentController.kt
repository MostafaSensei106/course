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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Enrollments", description = "Student enrollment and progress tracking")
@RestController
@RequestMapping(ApiRoutes.Enrollments.BASE)
class EnrollmentController(
    private val enrollUseCase: EnrollUseCase,
    private val myEnrollmentsUseCase: MyEnrollmentsUseCase,
    private val progressUseCase: UpdateProgressUseCase,
    private val cancelUseCase: CancelEnrollmentUseCase
) {
    @Operation(summary = "Enroll in course", description = "Enrolls the authenticated student in a published course")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "Enrolled successfully"),
        SwaggerApiResponse(responseCode = "409", description = "Already enrolled"),
        SwaggerApiResponse(responseCode = "400", description = "Course is not published"),
        SwaggerApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
        SwaggerApiResponse(responseCode = "404", description = "Course not found")
    )
    @PostMapping
    suspend fun enroll(@Valid @RequestBody req: EnrollRequest): ResponseEntity<ApiResponse<EnrollmentResponse>> =
        enrollUseCase(CurrentUser.requireId() to req.courseId).fold(
            onSuccess = { Responser.created(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("ENROLL_FAILED"), f.message) }
        )

    @Operation(summary = "My enrollments", description = "Returns all enrollments of the authenticated student")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Enrollments returned"),
        SwaggerApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    )
    @GetMapping(ApiRoutes.Enrollments.ME)
    suspend fun my(): ResponseEntity<ApiResponse<List<EnrollmentResponse>>> =
        myEnrollmentsUseCase(CurrentUser.requireId()).fold(
            onSuccess = { Responser.ok(it.map { e -> e.toResponse() }) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_ENROLLMENTS_FAILED"), f.message) }
        )

    @Operation(summary = "Update progress", description = "Updates learning progress (0-100) of an enrollment")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Progress updated"),
        SwaggerApiResponse(responseCode = "404", description = "Enrollment not found"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid progress value")
    )
    @PatchMapping(ApiRoutes.Enrollments.PROGRESS)
    suspend fun updateProgress(@PathVariable id: UUID, @Valid @RequestBody req: UpdateProgressRequest): ResponseEntity<ApiResponse<EnrollmentResponse>> =
        progressUseCase(id to req.progressPercent).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_PROGRESS_FAILED"), f.message) }
        )

    @Operation(summary = "Cancel enrollment", description = "Cancels an enrollment")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Enrollment cancelled"),
        SwaggerApiResponse(responseCode = "404", description = "Enrollment not found")
    )
    @DeleteMapping(ApiRoutes.Enrollments.BY_ID)
    suspend fun cancel(@PathVariable id: UUID): ResponseEntity<ApiResponse<EnrollmentResponse>> =
        cancelUseCase(id).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("CANCEL_ENROLLMENT_FAILED"), f.message) }
        )
}
