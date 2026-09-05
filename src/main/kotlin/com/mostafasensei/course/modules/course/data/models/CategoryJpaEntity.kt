package com.mostafasensei.course.modules.course.data.models

import com.mostafasensei.course.modules.course.domain.entity.Category
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "categories", uniqueConstraints = [UniqueConstraint(columnNames = ["slug"])])
class CategoryJpaEntity(
    @Id val id: UUID,
    @Column(nullable = false) val name: String,
    @Column(nullable = false, unique = true) val slug: String,
    @Column(columnDefinition = "TEXT") val description: String?,
    @Column(nullable = false) val createdAt: Instant,
    @Column(nullable = false) val updatedAt: Instant
) {
    protected constructor() : this(UUID.randomUUID(), "", "", null, Instant.now(), Instant.now())

    fun toDomain() = Category(id, name, slug, description, createdAt, updatedAt)

    companion object {
        fun fromDomain(c: Category) = CategoryJpaEntity(c.id, c.name, c.slug, c.description, c.createdAt, c.updatedAt)
    }
}
