package com.mostafasensei.course.modules.auth.domain.repository_impl

import com.mostafasensei.course.core.utils.result.Executor
import com.mostafasensei.course.core.utils.result.StorageResult
import com.mostafasensei.course.modules.auth.data.models.UserJpaEntity
import com.mostafasensei.course.modules.auth.data.repository.SpringJpaUserRepository
import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.entity.User
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


@Repository
class UserRepositoryImpl(
    private val jpaRepo: SpringJpaUserRepository
) : UserRepository {

    // Simple in-memory reset-token store (userId <-> token).
    // Keeps the app runnable without an extra table/migration.
    private val resetTokens = ConcurrentHashMap<String, UUID>()

    override suspend fun findById(id: UUID): StorageResult<User> = Executor.execute {
        val e = jpaRepo.findById(id).orElseThrow {
            NoSuchElementException("User not found with id: $id")
        }
        e.toDomain()
    }

    override suspend fun findByUsername(username: String): StorageResult<User> = findByEmail(username)

    override suspend fun findByEmail(email: String): StorageResult<User> = Executor.execute {
        val e = jpaRepo.findByEmail(email).orElseThrow {
            NoSuchElementException("User not found with email: $email")
        }
        e.toDomain()
    }

    override suspend fun existsByEmail(email: String): StorageResult<Boolean> = Executor.execute {
        jpaRepo.existsByEmail(email)
    }

    override suspend fun save(user: User): StorageResult<User> = Executor.execute {
        val e = UserJpaEntity.fromDomain(user)
        jpaRepo.save(e).toDomain()
    }

    override suspend fun savePasswordResetToken(userId: UUID, token: String): StorageResult<Unit> =
        Executor.execute {
            resetTokens[token] = userId
        }

    override suspend fun findUserIdByResetToken(token: String): StorageResult<UUID> =
        Executor.execute {
            resetTokens[token] ?: throw NoSuchElementException("Invalid or expired reset token")
        }

    override suspend fun clearResetToken(userId: UUID): StorageResult<Unit> = Executor.execute {
        resetTokens.entries.removeIf { it.value == userId }
        Unit
    }
}
