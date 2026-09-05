package com.mostafasensei.course.modules.course.data.models

import com.mostafasensei.course.modules.course.domain.entity.CourseSection
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "course_sections")
class SectionJpaEntity(
    @Id val id: UUID,
    @Column(nullable = false) val courseId: UUID,
    @Column(nullable = false) val title: String,
    @Column(nullable = false) val orderIndex: Int,
    @Column(nullable = false) val createdAt: Instant,
    @Column(nullable = false) val updatedAt: Instant
) {
    protected constructor() : this(UUID.randomUUID(), UUID.randomUUID(), "", 0, Instant.now(), Instant.now())

    fun toDomain() = CourseSection(id, courseId, title, orderIndex, createdAt, updatedAt)

    companion object {
        fun fromDomain(s: CourseSection) = SectionJpaEntity(s.id, s.courseId, s.title, s.orderIndex, s.createdAt, s.updatedAt)
    }
}
