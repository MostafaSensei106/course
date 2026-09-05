package com.mostafasensei.course.modules.auth.handler.usecase

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Result
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.core.utils.usecase.UseCaseBase
import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.security.PasswordHasher
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

data class ResetPasswordParams(
    val token: String,
    val newPassword: String
)

@Service
class RequestPasswordResetUseCase(
    private val userRepository: UserRepository,
) : UseCaseBase<Unit, String> {
    override suspend fun invoke(params: String): Result<Unit, Failures> {
        return userRepository.findByEmail(params).fold(
            onSuccess = { user ->
                val resetToken = UUID.randomUUID().toString()
                userRepository.savePasswordResetToken(user.id, resetToken)
            },
            onFailure = { failure -> Result.failure(failure) },
        )
    }
}

@Service
class ResetPasswordUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
) : UseCaseBase<Unit, ResetPasswordParams> {
    override suspend fun invoke(params: ResetPasswordParams): Result<Unit, Failures> {
        return userRepository.findUserIdByResetToken(params.token).fold(
            onSuccess = { userId ->
                userRepository.findById(userId).fold(
                    onSuccess = { user ->
                        val updatedUser = user.copy(
                            passwordHash = requireNotNull(passwordHasher.hash(params.newPassword)),
                            updatedAt = Instant.now()
                        )
                        userRepository.save(updatedUser).fold(
                            onSuccess = {
                                userRepository.clearResetToken(userId)
                            },
                            onFailure = { failure -> Result.failure(failure) }
                        )
                    },
                    onFailure = { failure -> Result.failure(failure) }
                )
            },
            onFailure = { failure -> Result.failure(failure) },
        )
    }
}
