package com.mostafasensei.course.modules.auth.data.repository

import com.mostafasensei.course.core.utils.result.StorageResult
import com.mostafasensei.course.modules.auth.domain.entity.User
import java.util.UUID


interface UserRepository {
    suspend fun findById(id: UUID): StorageResult<User>
    suspend fun findByUsername(username: String): StorageResult<User>
    suspend fun findByEmail(email: String): StorageResult<User>
    suspend fun existsByEmail(email: String): StorageResult<Boolean>
    suspend  fun save(user: User): StorageResult<User>
    suspend fun savePasswordResetToken(userId: UUID, token: String): StorageResult<Unit>
    suspend fun findUserIdByResetToken(token: String): StorageResult<UUID>
    suspend fun clearResetToken(userId: UUID): StorageResult<Unit>
}