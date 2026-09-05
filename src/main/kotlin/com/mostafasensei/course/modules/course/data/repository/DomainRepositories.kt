package com.mostafasensei.course.modules.course.data.repository

import com.mostafasensei.course.core.utils.result.StorageResult
import com.mostafasensei.course.modules.course.domain.entity.*
import java.util.UUID

interface CategoryRepository {
    suspend fun save(c: Category): StorageResult<Category>
    suspend fun findById(id: UUID): StorageResult<Category>
    suspend fun findBySlug(slug: String): StorageResult<Category>
    suspend fun findAll(): StorageResult<List<Category>>
    suspend fun deleteById(id: UUID): StorageResult<Unit>
    suspend fun existsBySlug(slug: String): StorageResult<Boolean>
}

interface CourseRepository {
    suspend fun save(c: Course): StorageResult<Course>
    suspend fun findById(id: UUID): StorageResult<Course>
    suspend fun findBySlug(slug: String): StorageResult<Course>
    suspend fun findPublished(page: Int, size: Int, categoryId: UUID?, search: String?): StorageResult<List<Course>>
    suspend fun findAllAdmin(page: Int, size: Int, status: CourseStatus?): StorageResult<List<Course>>
    suspend fun findByInstructor(instructorId: UUID): StorageResult<List<Course>>
    suspend fun deleteById(id: UUID): StorageResult<Unit>
    suspend fun existsBySlug(slug: String): StorageResult<Boolean>
}

interface SectionRepository {
    suspend fun save(s: CourseSection): StorageResult<CourseSection>
    suspend fun findById(id: UUID): StorageResult<CourseSection>
    suspend fun findByCourse(courseId: UUID): StorageResult<List<CourseSection>>
    suspend fun deleteById(id: UUID): StorageResult<Unit>
}

interface LessonRepository {
    suspend fun save(l: Lesson): StorageResult<Lesson>
    suspend fun findById(id: UUID): StorageResult<Lesson>
    suspend fun findBySection(sectionId: UUID): StorageResult<List<Lesson>>
    suspend fun findBySections(sectionIds: List<UUID>): StorageResult<List<Lesson>>
    suspend fun deleteById(id: UUID): StorageResult<Unit>
}

interface EnrollmentRepository {
    suspend fun save(e: Enrollment): StorageResult<Enrollment>
    suspend fun findById(id: UUID): StorageResult<Enrollment>
    suspend fun findByStudentAndCourse(studentId: UUID, courseId: UUID): StorageResult<Enrollment?>
    suspend fun findByStudent(studentId: UUID): StorageResult<List<Enrollment>>
    suspend fun findByCourse(courseId: UUID): StorageResult<List<Enrollment>>
    suspend fun deleteById(id: UUID): StorageResult<Unit>
}
