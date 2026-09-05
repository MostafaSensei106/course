package com.mostafasensei.course.modules.auth.data.models

import com.mostafasensei.course.modules.auth.domain.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.data.annotation.Id
import java.util.*

@Entity
@Table(name = "users")
class UserJapEntity(
    @Id
    val id: UUID,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val passwordHash: String,

    @Column(nullable = false)
    val firstName: String,

    @Column(nullable = false)
    val lastName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,

    @Column(nullable = false)
    val isVerified: Boolean,

    @Column(nullable = false)
    val isActive: Boolean,

    @Column(nullable = false)
    val createdAt: Instant,

    @Column(nullable = false)
    val updatedAt: Instant,

    @Column(nullable = true)
    val deletedAt: Instant? = null

){
    fun toDomain(): User = User(
        id = id,
        email = email,
        passwordHash = passwordHash,
        firstName = firstName,
        lastName = lastName,
        role = role,
        isVerified = isVerified,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
    companion object {
        fun fromDomain(user: User): UserJpaEntity = UserJpaEntity(
            id = user.id,
            email = user.email,
            passwordHash = user.passwordHash,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role,
            isVerified = user.isVerified,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            deletedAt = user.deletedAt
        )
    }
    }
}