package com.mostafasensei.course.modules.course.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.delivery.toCode
import com.mostafasensei.course.core.delivery.toHttpStatus
import com.mostafasensei.course.core.router.ApiRoutes
import com.mostafasensei.course.core.security.CurrentUser
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.modules.course.domain.entity.CourseStatus
import com.mostafasensei.course.modules.course.handler.dto.*
import com.mostafasensei.course.modules.course.handler.usecase.*
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ---------------- Public course fetches ----------------
@Tag(name = "Courses", description = "Public course catalog browsing")
@RestController
@RequestMapping(ApiRoutes.Courses.BASE)
class CourseController(
    private val listPublishedUseCase: ListPublishedCoursesUseCase,
    private val getDetailsUseCase: GetCourseDetailsUseCase,
    private val getBySlugUseCase: GetCourseBySlugUseCase
) {
    @Operation(summary = "List published courses", description = "Returns paginated published courses, optionally filtered by category or search text")
    @ApiResponses(SwaggerApiResponse(responseCode = "200", description = "Courses returned"))
    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<List<CourseResponse>>> =
        listPublishedUseCase(ListCoursesParams(page, size, categoryId, search)).fold(
            onSuccess = { Responser.ok(it.map { c -> c.toResponse() }) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_COURSES_FAILED"), f.message) }
        )

    @Operation(summary = "Get course details", description = "Returns a published course with its sections and lessons, by slug or UUID")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Course details returned"),
        SwaggerApiResponse(responseCode = "404", description = "Course not found")
    )
    @GetMapping(ApiRoutes.Courses.BY_SLUG_OR_ID)
    suspend fun details(@PathVariable slugOrId: String): ResponseEntity<ApiResponse<CourseDetailsResponse>> {
        val id = runCatching { UUID.fromString(slugOrId) }.getOrNull()
        if (id != null) return detailsById(id)
        return getBySlugUseCase(slugOrId).fold(
            onSuccess = { course -> detailsById(course.id) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("GET_COURSE_FAILED"), f.message) }
        )
    }

    @Operation(summary = "Get course details by id", description = "Returns a published course with its sections and lessons, by UUID")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Course details returned"),
        SwaggerApiResponse(responseCode = "404", description = "Course not found")
    )
    @GetMapping(ApiRoutes.Courses.BY_ID)
    suspend fun detailsById(@PathVariable id: UUID): ResponseEntity<ApiResponse<CourseDetailsResponse>> =
        getDetailsUseCase(id).fold(
            onSuccess = { d ->
                if (d.course.status != CourseStatus.PUBLISHED)
                    Responser.error(org.springframework.http.HttpStatus.NOT_FOUND, "NOT_FOUND", "Course not found")
                else Responser.ok(
                    CourseDetailsResponse(
                        d.course.toResponse(),
                        d.sections.map { s -> SectionResponse(s.section.id, s.section.title, s.section.orderIndex, s.lessons.map { it.toResponse() }) }
                    )
                )
            },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("GET_COURSE_FAILED"), f.message) }
        )
}

// ---------------- Admin / Instructor course CRUD ----------------
@Tag(name = "Admin - Courses", description = "Instructor/admin endpoints for managing courses")
@RestController
@RequestMapping(ApiRoutes.AdminCourses.BASE)
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MIN_ADMIN','INSTRUCTOR')")
class AdminCourseController(
    private val createCourseUseCase: CreateCourseUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val publishCourseUseCase: PublishCourseUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase,
    private val listAdminUseCase: ListAdminCoursesUseCase,
    private val getDetailsUseCase: GetCourseDetailsUseCase
) {
    @Operation(summary = "Create course", description = "Creates a new course as draft for the authenticated instructor")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "Course created"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        SwaggerApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    )
    @PostMapping
    suspend fun create(@Valid @RequestBody req: CreateCourseRequest): ResponseEntity<ApiResponse<CourseResponse>> =
        createCourseUseCase(CreateCourseParams(req.title, req.description, req.price, req.thumbnailUrl, req.categoryId, CurrentUser.requireId())).fold(
            onSuccess = { Responser.created(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("CREATE_COURSE_FAILED"), f.message) }
        )

    @Operation(summary = "List courses (admin)", description = "Returns paginated courses of any status, optionally filtered by status")
    @ApiResponses(SwaggerApiResponse(responseCode = "200", description = "Courses returned"))
    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: CourseStatus?
    ): ResponseEntity<ApiResponse<List<CourseResponse>>> =
        listAdminUseCase(ListAdminCoursesParams(page, size, status)).fold(
            onSuccess = { Responser.ok(it.map { c -> c.toResponse() }) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_COURSES_FAILED"), f.message) }
        )

    @Operation(summary = "Get course (admin)", description = "Returns a course with sections and lessons regardless of status")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Course details returned"),
        SwaggerApiResponse(responseCode = "404", description = "Course not found")
    )
    @GetMapping(ApiRoutes.AdminCourses.BY_ID)
    suspend fun get(@PathVariable id: UUID): ResponseEntity<ApiResponse<CourseDetailsResponse>> =
        getDetailsUseCase(id).fold(
            onSuccess = { d ->
                Responser.ok(
                    CourseDetailsResponse(
                        d.course.toResponse(),
                        d.sections.map { s -> SectionResponse(s.section.id, s.section.title, s.section.orderIndex, s.lessons.map { it.toResponse() }) }
                    )
                )
            },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("GET_COURSE_FAILED"), f.message) }
        )

    @Operation(summary = "Update course", description = "Updates title, description, price, thumbnail, category or status")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Course updated"),
        SwaggerApiResponse(responseCode = "404", description = "Course not found")
    )
    @PutMapping(ApiRoutes.AdminCourses.BY_ID)
    suspend fun update(@PathVariable id: UUID, @RequestBody req: UpdateCourseRequest): ResponseEntity<ApiResponse<CourseResponse>> =
        updateCourseUseCase(UpdateCourseParams(id, req.title, req.description, req.price, req.thumbnailUrl, req.categoryId, req.status)).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_COURSE_FAILED"), f.message) }
        )

    @Operation(summary = "Publish course", description = "Moves a course from draft to published")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Course published"),
        SwaggerApiResponse(responseCode = "404", description = "Course not found")
    )
    @PostMapping(ApiRoutes.AdminCourses.PUBLISH)
    suspend fun publish(@PathVariable id: UUID): ResponseEntity<ApiResponse<CourseResponse>> =
        publishCourseUseCase(id).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("PUBLISH_COURSE_FAILED"), f.message) }
        )

    @Operation(summary = "Delete course", description = "Deletes a course with its sections and lessons (ADMIN only)")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Course deleted"),
        SwaggerApiResponse(responseCode = "404", description = "Course not found"),
        SwaggerApiResponse(responseCode = "403", description = "Insufficient privileges")
    )
    @DeleteMapping(ApiRoutes.AdminCourses.BY_ID)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    suspend fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Map<String, String>>> =
        deleteCourseUseCase(id).fold(
            onSuccess = { Responser.ok(mapOf("message" to "Course deleted")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DELETE_COURSE_FAILED"), f.message) }
        )
}
