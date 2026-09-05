package com.mostafasensei.course.modules.course.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.delivery.toCode
import com.mostafasensei.course.core.delivery.toHttpStatus
import com.mostafasensei.course.core.router.ApiRoutes
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.modules.course.data.repository.LessonRepository
import com.mostafasensei.course.modules.course.data.repository.SectionRepository
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

@Tag(name = "Admin - Sections", description = "Instructor/admin endpoints for managing course sections")
@RestController
@RequestMapping(ApiRoutes.AdminSections.BASE)
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MIN_ADMIN','INSTRUCTOR')")
class AdminSectionController(
    private val createSectionUseCase: CreateSectionUseCase,
    private val updateSectionUseCase: UpdateSectionUseCase,
    private val deleteSectionUseCase: DeleteSectionUseCase,
    private val sectionRepo: SectionRepository,
    private val lessonRepo: LessonRepository
) {
    @Operation(summary = "List sections", description = "Returns all sections of a course with their lessons")
    @ApiResponses(SwaggerApiResponse(responseCode = "200", description = "Sections returned"))
    @GetMapping
    suspend fun list(@PathVariable courseId: UUID): ResponseEntity<ApiResponse<List<SectionResponse>>> =
        sectionRepo.findByCourse(courseId).fold(
            onSuccess = { secs ->
                val ids = secs.map { it.id }
                val all = lessonRepo.findBySections(ids).fold(onSuccess = { it }, onFailure = { emptyList() })
                Responser.ok(secs.map { s -> s.toResponse(all.filter { it.sectionId == s.id }) })
            },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_SECTIONS_FAILED"), f.message) }
        )

    @Operation(summary = "Create section", description = "Adds a new section to a course")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "Section created"),
        SwaggerApiResponse(responseCode = "404", description = "Course not found"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error")
    )
    @PostMapping
    suspend fun create(@PathVariable courseId: UUID, @Valid @RequestBody req: CreateSectionRequest): ResponseEntity<ApiResponse<SectionResponse>> =
        createSectionUseCase(CreateSectionParams(courseId, req.title, req.orderIndex)).fold(
            onSuccess = { Responser.created(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("CREATE_SECTION_FAILED"), f.message) }
        )

    @Operation(summary = "Update section", description = "Updates title or order of a section")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Section updated"),
        SwaggerApiResponse(responseCode = "404", description = "Section not found")
    )
    @PutMapping(ApiRoutes.AdminSections.BY_ID)
    suspend fun update(@PathVariable courseId: UUID, @PathVariable sectionId: UUID, @RequestBody req: UpdateSectionRequest): ResponseEntity<ApiResponse<SectionResponse>> =
        updateSectionUseCase(UpdateSectionParams(sectionId, req.title, req.orderIndex)).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_SECTION_FAILED"), f.message) }
        )

    @Operation(summary = "Delete section", description = "Deletes a section with its lessons")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Section deleted"),
        SwaggerApiResponse(responseCode = "404", description = "Section not found")
    )
    @DeleteMapping(ApiRoutes.AdminSections.BY_ID)
    suspend fun delete(@PathVariable courseId: UUID, @PathVariable sectionId: UUID): ResponseEntity<ApiResponse<Map<String, String>>> =
        deleteSectionUseCase(sectionId).fold(
            onSuccess = { Responser.ok(mapOf("message" to "Section deleted")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DELETE_SECTION_FAILED"), f.message) }
        )
}

@Tag(name = "Admin - Lessons", description = "Instructor/admin endpoints for managing section lessons")
@RestController
@RequestMapping(ApiRoutes.AdminLessons.BASE)
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MIN_ADMIN','INSTRUCTOR')")
class AdminLessonController(
    private val createLessonUseCase: CreateLessonUseCase,
    private val updateLessonUseCase: UpdateLessonUseCase,
    private val deleteLessonUseCase: DeleteLessonUseCase,
    private val lessonRepo: LessonRepository
) {
    @Operation(summary = "List lessons", description = "Returns all lessons of a section")
    @ApiResponses(SwaggerApiResponse(responseCode = "200", description = "Lessons returned"))
    @GetMapping
    suspend fun list(@PathVariable sectionId: UUID): ResponseEntity<ApiResponse<List<LessonResponse>>> =
        lessonRepo.findBySection(sectionId).fold(
            onSuccess = { Responser.ok(it.map { l -> l.toResponse() }) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_LESSONS_FAILED"), f.message) }
        )

    @Operation(summary = "Create lesson", description = "Adds a new lesson to a section")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "Lesson created"),
        SwaggerApiResponse(responseCode = "404", description = "Section not found"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error")
    )
    @PostMapping
    suspend fun create(@PathVariable sectionId: UUID, @Valid @RequestBody req: CreateLessonRequest): ResponseEntity<ApiResponse<LessonResponse>> =
        createLessonUseCase(CreateLessonParams(sectionId, req.title, req.contentType, req.contentUrl, req.textContent, req.durationSeconds, req.orderIndex, req.isPreview)).fold(
            onSuccess = { Responser.created(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("CREATE_LESSON_FAILED"), f.message) }
        )

    @Operation(summary = "Update lesson", description = "Updates content or metadata of a lesson")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Lesson updated"),
        SwaggerApiResponse(responseCode = "404", description = "Lesson not found")
    )
    @PutMapping(ApiRoutes.AdminLessons.BY_ID)
    suspend fun update(@PathVariable sectionId: UUID, @PathVariable lessonId: UUID, @RequestBody req: UpdateLessonRequest): ResponseEntity<ApiResponse<LessonResponse>> =
        updateLessonUseCase(UpdateLessonParams(lessonId, req.title, req.contentType, req.contentUrl, req.textContent, req.durationSeconds, req.orderIndex, req.isPreview)).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_LESSON_FAILED"), f.message) }
        )

    @Operation(summary = "Delete lesson", description = "Deletes a lesson permanently")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Lesson deleted"),
        SwaggerApiResponse(responseCode = "404", description = "Lesson not found")
    )
    @DeleteMapping(ApiRoutes.AdminLessons.BY_ID)
    suspend fun delete(@PathVariable sectionId: UUID, @PathVariable lessonId: UUID): ResponseEntity<ApiResponse<Map<String, String>>> =
        deleteLessonUseCase(lessonId).fold(
            onSuccess = { Responser.ok(mapOf("message" to "Lesson deleted")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DELETE_LESSON_FAILED"), f.message) }
        )
}
