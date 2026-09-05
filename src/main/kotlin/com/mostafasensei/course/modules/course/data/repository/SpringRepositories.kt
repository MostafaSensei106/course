package com.mostafasensei.course.modules.course.data.repository

import com.mostafasensei.course.modules.course.data.models.*
import com.mostafasensei.course.modules.course.domain.entity.CourseStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface SpringCategoryRepository : JpaRepository<CategoryJpaEntity, UUID> {
    fun findBySlug(slug: String): Optional<CategoryJpaEntity>
    fun existsBySlug(slug: String): Boolean
}

interface SpringCourseRepository : JpaRepository<CourseJpaEntity, UUID> {
    fun findBySlug(slug: String): Optional<CourseJpaEntity>
    fun existsBySlug(slug: String): Boolean
    fun findByStatus(status: CourseStatus, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<CourseJpaEntity>
    fun findByStatusAndCategoryId(status: CourseStatus, categoryId: UUID, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<CourseJpaEntity>
    @Query("select c from CourseJpaEntity c where c.status = :status and (lower(c.title) like lower(concat('%', :q, '%')) or lower(c.description) like lower(concat('%', :q, '%')))")
    fun searchPublished(status: CourseStatus, q: String, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<CourseJpaEntity>
    fun findByInstructorId(instructorId: UUID): List<CourseJpaEntity>
}

interface SpringSectionRepository : JpaRepository<SectionJpaEntity, UUID> {
    fun findByCourseIdOrderByOrderIndexAsc(courseId: UUID): List<SectionJpaEntity>
}

interface SpringLessonRepository : JpaRepository<LessonJpaEntity, UUID> {
    fun findBySectionIdOrderByOrderIndexAsc(sectionId: UUID): List<LessonJpaEntity>
    fun findBySectionIdIn(sectionIds: List<UUID>): List<LessonJpaEntity>
}

interface SpringEnrollmentRepository : JpaRepository<EnrollmentJpaEntity, UUID> {
    fun findByStudentIdAndCourseId(studentId: UUID, courseId: UUID): Optional<EnrollmentJpaEntity>
    fun findByStudentId(studentId: UUID): List<EnrollmentJpaEntity>
    fun findByCourseId(courseId: UUID): List<EnrollmentJpaEntity>
}
