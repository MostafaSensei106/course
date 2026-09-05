package com.mostafasensei.course.modules.auth.data.repository

import com.mostafasensei.course.modules.auth.data.models.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface SpringUserRepository : JpaRepository<UserJpaEntity, UUID> {
    fun findByEmail(email: String): Optional<UserJpaEntity>
    fun findByUsername(username: String): Optional<UserJpaEntity>
}