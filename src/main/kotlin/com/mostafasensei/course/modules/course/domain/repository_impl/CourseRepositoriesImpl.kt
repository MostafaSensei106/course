package com.mostafasensei.course.modules.course.domain.repository_impl

import com.mostafasensei.course.core.utils.result.Executor
import com.mostafasensei.course.core.utils.result.StorageResult
import com.mostafasensei.course.modules.course.data.models.*
import com.mostafasensei.course.modules.course.data.repository.*
import com.mostafasensei.course.modules.course.domain.entity.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CategoryRepositoryImpl(private val jpa: SpringCategoryRepository) : CategoryRepository {
    override suspend fun save(c: Category): StorageResult<Category> = Executor.execute { jpa.save(CategoryJpaEntity.fromDomain(c)).toDomain() }
    override suspend fun findById(id: UUID): StorageResult<Category> = Executor.execute {
        jpa.findById(id).orElseThrow { NoSuchElementException("Category not found: $id") }.toDomain()
    }
    override suspend fun findBySlug(slug: String): StorageResult<Category> = Executor.execute {
        jpa.findBySlug(slug).orElseThrow { NoSuchElementException("Category not found: $slug") }.toDomain()
    }
    override suspend fun findAll(): StorageResult<List<Category>> = Executor.execute {
        jpa.findAll(Sort.by("name")).map { it.toDomain() }
    }
    override suspend fun deleteById(id: UUID): StorageResult<Unit> = Executor.execute { jpa.deleteById(id) }
    override suspend fun existsBySlug(slug: String): StorageResult<Boolean> = Executor.execute { jpa.existsBySlug(slug) }
}

@Repository
class CourseRepositoryImpl(private val jpa: SpringCourseRepository) : CourseRepository {
    override suspend fun save(c: Course): StorageResult<Course> = Executor.execute { jpa.save(CourseJpaEntity.fromDomain(c)).toDomain() }
    override suspend fun findById(id: UUID): StorageResult<Course> = Executor.execute {
        jpa.findById(id).orElseThrow { NoSuchElementException("Course not found: $id") }.toDomain()
    }
    override suspend fun findBySlug(slug: String): StorageResult<Course> = Executor.execute {
        jpa.findBySlug(slug).orElseThrow { NoSuchElementException("Course not found: $slug") }.toDomain()
    }
    override suspend fun findPublished(page: Int, size: Int, categoryId: UUID?, search: String?): StorageResult<List<Course>> =
        Executor.execute {
            val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 50), Sort.by("createdAt").descending())
            val result = when {
                !search.isNullOrBlank() -> jpa.searchPublished(CourseStatus.PUBLISHED, search, pageable)
                categoryId != null -> jpa.findByStatusAndCategoryId(CourseStatus.PUBLISHED, categoryId, pageable)
                else -> jpa.findByStatus(CourseStatus.PUBLISHED, pageable)
            }
            result.content.map { it.toDomain() }
        }
    override suspend fun findAllAdmin(page: Int, size: Int, status: CourseStatus?): StorageResult<List<Course>> =
        Executor.execute {
            val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), Sort.by("createdAt").descending())
            val result = if (status != null) jpa.findByStatus(status, pageable) else jpa.findAll(pageable)
            result.content.map { it.toDomain() }
        }
    override suspend fun findByInstructor(instructorId: UUID): StorageResult<List<Course>> = Executor.execute {
        jpa.findByInstructorId(instructorId).map { it.toDomain() }
    }
    override suspend fun deleteById(id: UUID): StorageResult<Unit> = Executor.execute { jpa.deleteById(id) }
    override suspend fun existsBySlug(slug: String): StorageResult<Boolean> = Executor.execute { jpa.existsBySlug(slug) }
}

@Repository
class SectionRepositoryImpl(private val jpa: SpringSectionRepository) : SectionRepository {
    override suspend fun save(s: CourseSection): StorageResult<CourseSection> = Executor.execute { jpa.save(SectionJpaEntity.fromDomain(s)).toDomain() }
    override suspend fun findById(id: UUID): StorageResult<CourseSection> = Executor.execute {
        jpa.findById(id).orElseThrow { NoSuchElementException("Section not found: $id") }.toDomain()
    }
    override suspend fun findByCourse(courseId: UUID): StorageResult<List<CourseSection>> = Executor.execute {
        jpa.findByCourseIdOrderByOrderIndexAsc(courseId).map { it.toDomain() }
    }
    override suspend fun deleteById(id: UUID): StorageResult<Unit> = Executor.execute { jpa.deleteById(id) }
}

@Repository
class LessonRepositoryImpl(private val jpa: SpringLessonRepository) : LessonRepository {
    override suspend fun save(l: Lesson): StorageResult<Lesson> = Executor.execute { jpa.save(LessonJpaEntity.fromDomain(l)).toDomain() }
    override suspend fun findById(id: UUID): StorageResult<Lesson> = Executor.execute {
        jpa.findById(id).orElseThrow { NoSuchElementException("Lesson not found: $id") }.toDomain()
    }
    override suspend fun findBySection(sectionId: UUID): StorageResult<List<Lesson>> = Executor.execute {
        jpa.findBySectionIdOrderByOrderIndexAsc(sectionId).map { it.toDomain() }
    }
    override suspend fun findBySections(sectionIds: List<UUID>): StorageResult<List<Lesson>> = Executor.execute {
        if (sectionIds.isEmpty()) emptyList() else jpa.findBySectionIdIn(sectionIds).map { it.toDomain() }
    }
    override suspend fun deleteById(id: UUID): StorageResult<Unit> = Executor.execute { jpa.deleteById(id) }
}

@Repository
class EnrollmentRepositoryImpl(private val jpa: SpringEnrollmentRepository) : EnrollmentRepository {
    override suspend fun save(e: Enrollment): StorageResult<Enrollment> = Executor.execute { jpa.save(EnrollmentJpaEntity.fromDomain(e)).toDomain() }
    override suspend fun findById(id: UUID): StorageResult<Enrollment> = Executor.execute {
        jpa.findById(id).orElseThrow { NoSuchElementException("Enrollment not found: $id") }.toDomain()
    }
    override suspend fun findByStudentAndCourse(studentId: UUID, courseId: UUID): StorageResult<Enrollment?> =
        Executor.execute { jpa.findByStudentIdAndCourseId(studentId, courseId).map { it.toDomain() }.orElse(null) }
    override suspend fun findByStudent(studentId: UUID): StorageResult<List<Enrollment>> = Executor.execute {
        jpa.findByStudentId(studentId).map { it.toDomain() }
    }
    override suspend fun findByCourse(courseId: UUID): StorageResult<List<Enrollment>> = Executor.execute {
        jpa.findByCourseId(courseId).map { it.toDomain() }
    }
    override suspend fun deleteById(id: UUID): StorageResult<Unit> = Executor.execute { jpa.deleteById(id) }
}
