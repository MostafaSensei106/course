package com.mostafasensei.course.modules.course.data.models

import com.mostafasensei.course.modules.course.domain.entity.Enrollment
import com.mostafasensei.course.modules.course.domain.entity.EnrollmentStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = [UniqueConstraint(columnNames = ["studentId", "courseId"])]
)
class EnrollmentJpaEntity(
    @Id val id: UUID,
    @Column(nullable = false) val studentId: UUID,
    @Column(nullable = false) val courseId: UUID,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val status: EnrollmentStatus,
    @Column(nullable = false) val progressPercent: Int,
    @Column(nullable = false) val enrolledAt: Instant,
    @Column(nullable = false) val updatedAt: Instant
) {
    protected constructor() : this(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        EnrollmentStatus.ACTIVE, 0, Instant.now(), Instant.now()
    )

    fun toDomain() = Enrollment(id, studentId, courseId, status, progressPercent, enrolledAt, updatedAt)

    companion object {
        fun fromDomain(e: Enrollment) = EnrollmentJpaEntity(
            e.id, e.studentId, e.courseId, e.status, e.progressPercent, e.enrolledAt, e.updatedAt
        )
    }
}
