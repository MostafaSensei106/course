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
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

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

    @PostMapping
    suspend fun create(@PathVariable courseId: UUID, @Valid @RequestBody req: CreateSectionRequest): ResponseEntity<ApiResponse<SectionResponse>> =
        createSectionUseCase(CreateSectionParams(courseId, req.title, req.orderIndex)).fold(
            onSuccess = { Responser.created(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("CREATE_SECTION_FAILED"), f.message) }
        )

    @PutMapping(ApiRoutes.AdminSections.BY_ID)
    suspend fun update(@PathVariable courseId: UUID, @PathVariable sectionId: UUID, @RequestBody req: UpdateSectionRequest): ResponseEntity<ApiResponse<SectionResponse>> =
        updateSectionUseCase(UpdateSectionParams(sectionId, req.title, req.orderIndex)).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_SECTION_FAILED"), f.message) }
        )

    @DeleteMapping(ApiRoutes.AdminSections.BY_ID)
    suspend fun delete(@PathVariable courseId: UUID, @PathVariable sectionId: UUID): ResponseEntity<ApiResponse<Map<String, String>>> =
        deleteSectionUseCase(sectionId).fold(
            onSuccess = { Responser.ok(mapOf("message" to "Section deleted")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DELETE_SECTION_FAILED"), f.message) }
        )
}

@RestController
@RequestMapping(ApiRoutes.AdminLessons.BASE)
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MIN_ADMIN','INSTRUCTOR')")
class AdminLessonController(
    private val createLessonUseCase: CreateLessonUseCase,
    private val updateLessonUseCase: UpdateLessonUseCase,
    private val deleteLessonUseCase: DeleteLessonUseCase,
    private val lessonRepo: LessonRepository
) {
    @GetMapping
    suspend fun list(@PathVariable sectionId: UUID): ResponseEntity<ApiResponse<List<LessonResponse>>> =
        lessonRepo.findBySection(sectionId).fold(
            onSuccess = { Responser.ok(it.map { l -> l.toResponse() }) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_LESSONS_FAILED"), f.message) }
        )

    @PostMapping
    suspend fun create(@PathVariable sectionId: UUID, @Valid @RequestBody req: CreateLessonRequest): ResponseEntity<ApiResponse<LessonResponse>> =
        createLessonUseCase(CreateLessonParams(sectionId, req.title, req.contentType, req.contentUrl, req.textContent, req.durationSeconds, req.orderIndex, req.isPreview)).fold(
            onSuccess = { Responser.created(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("CREATE_LESSON_FAILED"), f.message) }
        )

    @PutMapping(ApiRoutes.AdminLessons.BY_ID)
    suspend fun update(@PathVariable sectionId: UUID, @PathVariable lessonId: UUID, @RequestBody req: UpdateLessonRequest): ResponseEntity<ApiResponse<LessonResponse>> =
        updateLessonUseCase(UpdateLessonParams(lessonId, req.title, req.contentType, req.contentUrl, req.textContent, req.durationSeconds, req.orderIndex, req.isPreview)).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_LESSON_FAILED"), f.message) }
        )

    @DeleteMapping(ApiRoutes.AdminLessons.BY_ID)
    suspend fun delete(@PathVariable sectionId: UUID, @PathVariable lessonId: UUID): ResponseEntity<ApiResponse<Map<String, String>>> =
        deleteLessonUseCase(lessonId).fold(
            onSuccess = { Responser.ok(mapOf("message" to "Lesson deleted")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DELETE_LESSON_FAILED"), f.message) }
        )
}
