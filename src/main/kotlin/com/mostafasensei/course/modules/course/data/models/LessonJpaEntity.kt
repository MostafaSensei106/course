package com.mostafasensei.course.modules.course.data.models

import com.mostafasensei.course.modules.course.domain.entity.Lesson
import com.mostafasensei.course.modules.course.domain.entity.LessonContentType
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "lessons")
class LessonJpaEntity(
    @Id val id: UUID,
    @Column(nullable = false) val sectionId: UUID,
    @Column(nullable = false) val title: String,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val contentType: LessonContentType,
    val contentUrl: String?,
    @Column(columnDefinition = "TEXT") val textContent: String?,
    val durationSeconds: Int?,
    @Column(nullable = false) val orderIndex: Int,
    @Column(nullable = false) val isPreview: Boolean,
    @Column(nullable = false) val createdAt: Instant,
    @Column(nullable = false) val updatedAt: Instant
) {
    protected constructor() : this(
        UUID.randomUUID(), UUID.randomUUID(), "", LessonContentType.VIDEO,
        null, null, null, 0, false, Instant.now(), Instant.now()
    )

    fun toDomain() = Lesson(id, sectionId, title, contentType, contentUrl, textContent, durationSeconds, orderIndex, isPreview, createdAt, updatedAt)

    companion object {
        fun fromDomain(l: Lesson) = LessonJpaEntity(
            l.id, l.sectionId, l.title, l.contentType, l.contentUrl, l.textContent,
            l.durationSeconds, l.orderIndex, l.isPreview, l.createdAt, l.updatedAt
        )
    }
}
