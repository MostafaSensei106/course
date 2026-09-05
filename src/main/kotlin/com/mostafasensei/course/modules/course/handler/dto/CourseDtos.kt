package com.mostafasensei.course.modules.course.handler.dto

import com.mostafasensei.course.modules.course.domain.entity.*
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.UUID

// ---------- Category ----------
data class CreateCategoryRequest(
    @field:Schema(example = "Programming", description = "Unique category name")
    @field:NotBlank val name: String,
    @field:Schema(example = "Courses about software development")
    val description: String? = null
)
data class UpdateCategoryRequest(
    @field:Schema(example = "Programming") val name: String?,
    @field:Schema(example = "Courses about software development") val description: String?
)
data class CategoryResponse(val id: UUID, val name: String, val slug: String, val description: String?)
fun Category.toResponse() = CategoryResponse(id, name, slug, description)

// ---------- Course ----------
data class CreateCourseRequest(
    @field:Schema(example = "Kotlin for Beginners", description = "Course title (slug is auto-generated)")
    @field:NotBlank val title: String,
    @field:Schema(example = "Learn Kotlin from zero to hero")
    val description: String? = null,
    @field:Schema(example = "49.99", description = "Price, 0 means free")
    @field:DecimalMin("0.0") val price: BigDecimal = BigDecimal.ZERO,
    @field:Schema(example = "https://cdn.example.com/kotlin.png")
    val thumbnailUrl: String? = null,
    @field:Schema(description = "Owning category UUID (optional)")
    val categoryId: UUID? = null
)
data class UpdateCourseRequest(
    @field:Schema(example = "Kotlin for Beginners") val title: String? = null,
    @field:Schema(example = "Learn Kotlin from zero to hero") val description: String? = null,
    @field:Schema(example = "49.99") val price: BigDecimal? = null,
    @field:Schema(example = "https://cdn.example.com/kotlin.png") val thumbnailUrl: String? = null,
    @field:Schema(description = "Owning category UUID") val categoryId: UUID? = null,
    @field:Schema(example = "PUBLISHED", description = "One of DRAFT, PUBLISHED, ARCHIVED") val status: CourseStatus? = null
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
data class CreateSectionRequest(
    @field:Schema(example = "Getting Started") @field:NotBlank val title: String,
    @field:Schema(example = "0", description = "Display order inside the course") @field:Min(0) val orderIndex: Int = 0
)
data class UpdateSectionRequest(
    @field:Schema(example = "Getting Started") val title: String?,
    @field:Schema(example = "1") val orderIndex: Int?
)
fun CourseSection.toResponse(lessons: List<Lesson> = emptyList()) =
    SectionResponse(id, title, orderIndex, lessons.sortedBy { it.orderIndex }.map { it.toResponse() })

// ---------- Lesson ----------
data class CreateLessonRequest(
    @field:Schema(example = "Install the JDK") @field:NotBlank val title: String,
    @field:Schema(example = "VIDEO", description = "One of VIDEO, TEXT, QUIZ, FILE") val contentType: LessonContentType = LessonContentType.VIDEO,
    @field:Schema(example = "https://cdn.example.com/lessons/1.mp4") val contentUrl: String? = null,
    @field:Schema(example = "Lesson notes in markdown...") val textContent: String? = null,
    @field:Schema(example = "600", description = "Duration in seconds") val durationSeconds: Int? = null,
    @field:Schema(example = "0") @field:Min(0) val orderIndex: Int = 0,
    @field:Schema(example = "true", description = "Free preview without enrollment") val isPreview: Boolean = false
)
data class UpdateLessonRequest(
    @field:Schema(example = "Install the JDK") val title: String? = null,
    @field:Schema(description = "One of VIDEO, TEXT, QUIZ, FILE") val contentType: LessonContentType? = null,
    val contentUrl: String? = null, val textContent: String? = null,
    val durationSeconds: Int? = null, val orderIndex: Int? = null, val isPreview: Boolean? = null
)

// ---------- Enrollment ----------
data class EnrollRequest(
    @field:Schema(description = "Published course UUID to enroll in") val courseId: UUID
)
data class UpdateProgressRequest(
    @field:Schema(example = "50", description = "Progress percent, 100 marks the enrollment completed")
    @field:Min(0) @field:Max(100) val progressPercent: Int
)
data class EnrollmentResponse(val id: UUID, val courseId: UUID, val status: EnrollmentStatus, val progressPercent: Int)
fun Enrollment.toResponse() = EnrollmentResponse(id, courseId, status, progressPercent)
