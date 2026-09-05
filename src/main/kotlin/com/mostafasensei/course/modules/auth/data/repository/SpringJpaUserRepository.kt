package com.mostafasensei.course.modules.auth.data.repository

import com.mostafasensei.course.core.utils.result.StorageResult
import com.mostafasensei.course.modules.auth.data.models.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface SpringJpaUserRepository : JpaRepository<UserJpaEntity, UUID> {
    fun findByEmail(email: String): Optional<UserJpaEntity>
    fun existsByEmail(email: String): Boolean
}