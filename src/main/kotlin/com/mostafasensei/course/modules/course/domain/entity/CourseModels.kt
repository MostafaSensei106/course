package com.mostafasensei.course.modules.course.domain.entity

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Category(
    val id: UUID,
    val name: String,
    val slug: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class CourseStatus { DRAFT, PUBLISHED, ARCHIVED }

data class Course(
    val id: UUID,
    val title: String,
    val slug: String,
    val description: String?,
    val price: BigDecimal,
    val thumbnailUrl: String?,
    val categoryId: UUID?,
    val instructorId: UUID,
    val status: CourseStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class CourseSection(
    val id: UUID,
    val courseId: UUID,
    val title: String,
    val orderIndex: Int,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class LessonContentType { VIDEO, TEXT, QUIZ, FILE }

data class Lesson(
    val id: UUID,
    val sectionId: UUID,
    val title: String,
    val contentType: LessonContentType,
    val contentUrl: String?,
    val textContent: String?,
    val durationSeconds: Int?,
    val orderIndex: Int,
    val isPreview: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class EnrollmentStatus { ACTIVE, COMPLETED, CANCELLED }

data class Enrollment(
    val id: UUID,
    val studentId: UUID,
    val courseId: UUID,
    val status: EnrollmentStatus,
    val progressPercent: Int,
    val enrolledAt: Instant,
    val updatedAt: Instant
)

data class SectionWithLessons(
    val section: CourseSection,
    val lessons: List<Lesson>
)

data class CourseDetails(
    val course: Course,
    val sections: List<SectionWithLessons>
)

fun slugify(input: String): String =
    input.lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .trim()
        .replace(Regex("\\s+"), "-")
        .replace(Regex("-+"), "-")
