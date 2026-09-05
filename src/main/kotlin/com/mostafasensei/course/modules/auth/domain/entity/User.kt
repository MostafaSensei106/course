package com.mostafasensei.course.modules.auth.domain.entity

import java.util.*
import kotlin.time.Instant

enum class UserRole {
    GUST,
    STUDENT,
    STUDENT_SUPERVISOR,
    INSTRUCTOR,
    MIN_ADMIN,
    ADMIN,
    SUPER_ADMIN
}

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isVerified: Boolean,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null
)
