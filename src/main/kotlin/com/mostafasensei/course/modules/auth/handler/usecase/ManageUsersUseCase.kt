package com.mostafasensei.course.modules.auth.handler.usecase

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Result
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.core.utils.usecase.UseCaseBase
import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.entity.User
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

data class ListUsersParams(val page: Int = 0, val size: Int = 20)

@Service
class ListUsersUseCase(
    private val userRepository: UserRepository,
    private val springRepo: com.mostafasensei.course.modules.auth.data.repository.SpringJpaUserRepository
) : UseCaseBase<List<User>, ListUsersParams> {
    override suspend fun invoke(params: ListUsersParams): Result<List<User>, Failures> {
        return com.mostafasensei.course.core.utils.result.Executor.execute {
            springRepo.findAll(
                PageRequest.of(params.page.coerceAtLeast(0), params.size.coerceIn(1, 100), Sort.by("createdAt").descending())
            ).content.map { it.toDomain() }
        }
    }
}

data class UpdateUserStatusParams(
    val userId: UUID,
    val isActive: Boolean?,
    val isVerified: Boolean?
)

@Service
class UpdateUserStatusUseCase(
    private val userRepository: UserRepository
) : UseCaseBase<User, UpdateUserStatusParams> {
    override suspend fun invoke(params: UpdateUserStatusParams): Result<User, Failures> {
        return userRepository.findById(params.userId).fold(
            onSuccess = { user ->
                val updated = user.copy(
                    isActive = params.isActive ?: user.isActive,
                    isVerified = params.isVerified ?: user.isVerified,
                    updatedAt = Instant.now()
                )
                userRepository.save(updated)
            },
            onFailure = { Result.failure(it) }
        )
    }
}

@Service
class DeleteUserUseCase(
    private val userRepository: UserRepository,
    private val springRepo: com.mostafasensei.course.modules.auth.data.repository.SpringJpaUserRepository
) : UseCaseBase<Unit, UUID> {
    override suspend fun invoke(params: UUID): Result<Unit, Failures> {
        return userRepository.findById(params).fold(
            onSuccess = {
                com.mostafasensei.course.core.utils.result.Executor.execute {
                    springRepo.deleteById(params)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}

@Service
class DeactivateUserUseCase(
    private val updateStatus: UpdateUserStatusUseCase
) : UseCaseBase<User, UUID> {
    override suspend fun invoke(params: UUID): Result<User, Failures> =
        updateStatus(UpdateUserStatusParams(params, isActive = false, isVerified = null))
}

@Service
class GetUserByIdUseCase(
    private val userRepository: UserRepository
) : UseCaseBase<User, UUID> {
    override suspend fun invoke(params: UUID): Result<User, Failures> = userRepository.findById(params)
}
