package com.mostafasensei.course.modules.course.handler.dto

import com.mostafasensei.course.modules.course.domain.entity.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.UUID

// ---------- Category ----------
data class CreateCategoryRequest(
    @field:NotBlank val name: String,
    val description: String? = null
)
data class UpdateCategoryRequest(val name: String?, val description: String?)
data class CategoryResponse(val id: UUID, val name: String, val slug: String, val description: String?)
fun Category.toResponse() = CategoryResponse(id, name, slug, description)

// ---------- Course ----------
data class CreateCourseRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    @field:DecimalMin("0.0") val price: BigDecimal = BigDecimal.ZERO,
    val thumbnailUrl: String? = null,
    val categoryId: UUID? = null
)
data class UpdateCourseRequest(
    val title: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val thumbnailUrl: String? = null,
    val categoryId: UUID? = null,
    val status: CourseStatus? = null
)
data class CourseResponse(
    val id: UUID, val title: String, val slug: String, val description: String?,
    val price: BigDecimal, val thumbnailUrl: String?, val categoryId: UUID?,
    val instructorId: UUID, val status: CourseStatus
)
fun Course.toResponse() = CourseResponse(id, title, slug, description, price, thumbnailUrl, categoryId, instructorId, status)

data class LessonResponse(
    val id: UUID, val title: String, val contentType: LessonContentType,
    val contentUrl: String?, val textContent: String?, val durationSeconds: Int?,
    val orderIndex: Int, val isPreview: Boolean
)
fun Lesson.toResponse() = LessonResponse(id, title, contentType, contentUrl, textContent, durationSeconds, orderIndex, isPreview)

data class SectionResponse(val id: UUID, val title: String, val orderIndex: Int, val lessons: List<LessonResponse>)
data class CourseDetailsResponse(val course: CourseResponse, val sections: List<SectionResponse>)

// ---------- Section ----------
data class CreateSectionRequest(@field:NotBlank val title: String, @field:Min(0) val orderIndex: Int = 0)
data class UpdateSectionRequest(val title: String?, val orderIndex: Int?)
fun CourseSection.toResponse(lessons: List<Lesson> = emptyList()) =
    SectionResponse(id, title, orderIndex, lessons.sortedBy { it.orderIndex }.map { it.toResponse() })

// ---------- Lesson ----------
data class CreateLessonRequest(
    @field:NotBlank val title: String,
    val contentType: LessonContentType = LessonContentType.VIDEO,
    val contentUrl: String? = null,
    val textContent: String? = null,
    val durationSeconds: Int? = null,
    @field:Min(0) val orderIndex: Int = 0,
    val isPreview: Boolean = false
)
data class UpdateLessonRequest(
    val title: String? = null, val contentType: LessonContentType? = null,
    val contentUrl: String? = null, val textContent: String? = null,
    val durationSeconds: Int? = null, val orderIndex: Int? = null, val isPreview: Boolean? = null
)

// ---------- Enrollment ----------
data class EnrollRequest(val courseId: UUID)
data class UpdateProgressRequest(@field:Min(0) @field:Max(100) val progressPercent: Int)
data class EnrollmentResponse(val id: UUID, val courseId: UUID, val status: EnrollmentStatus, val progressPercent: Int)
fun Enrollment.toResponse() = EnrollmentResponse(id, courseId, status, progressPercent)
