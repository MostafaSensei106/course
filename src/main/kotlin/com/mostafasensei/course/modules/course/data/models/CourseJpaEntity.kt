package com.mostafasensei.course.modules.course.data.models

import com.mostafasensei.course.modules.course.domain.entity.Course
import com.mostafasensei.course.modules.course.domain.entity.CourseStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "courses", uniqueConstraints = [UniqueConstraint(columnNames = ["slug"])])
class CourseJpaEntity(
    @Id val id: UUID,
    @Column(nullable = false) val title: String,
    @Column(nullable = false, unique = true) val slug: String,
    @Column(columnDefinition = "TEXT") val description: String?,
    @Column(nullable = false) val price: BigDecimal,
    val thumbnailUrl: String?,
    val categoryId: UUID?,
    @Column(nullable = false) val instructorId: UUID,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val status: CourseStatus,
    @Column(nullable = false) val createdAt: Instant,
    @Column(nullable = false) val updatedAt: Instant
) {
    protected constructor() : this(
        UUID.randomUUID(), "", "", null, BigDecimal.ZERO, null, null,
        UUID.randomUUID(), CourseStatus.DRAFT, Instant.now(), Instant.now()
    )

    fun toDomain() = Course(id, title, slug, description, price, thumbnailUrl, categoryId, instructorId, status, createdAt, updatedAt)

    companion object {
        fun fromDomain(c: Course) = CourseJpaEntity(
            c.id, c.title, c.slug, c.description, c.price, c.thumbnailUrl,
            c.categoryId, c.instructorId, c.status, c.createdAt, c.updatedAt
        )
    }
}
