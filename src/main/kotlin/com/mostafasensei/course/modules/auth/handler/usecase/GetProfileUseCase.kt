package com.mostafasensei.course.modules.auth.handler.usecase

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Result
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.core.utils.usecase.UseCaseBase
import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.entity.User
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class GetUserProfileUseCase(
    private val userRepository: UserRepository
) : UseCaseBase<User, UUID> {
    override suspend operator fun invoke(params: UUID): Result<User, Failures> =
        userRepository.findById(params)
}

data class UpdateProfileParams(
    val userId: UUID,
    val firstName: String,
    val lastName: String
)

@Service
class UpdateUserProfileUseCase(
    private val userRepository: UserRepository
) : UseCaseBase<User, UpdateProfileParams> {

    override suspend operator fun invoke(params: UpdateProfileParams): Result<User, Failures> {
        return userRepository.findById(params.userId).fold(
            onSuccess = { currentUser ->
                val updatedUser = currentUser.copy(
                    firstName = params.firstName,
                    lastName = params.lastName,
                    updatedAt = Instant.now()
                )
                userRepository.save(updatedUser)
            },
            onFailure = { failure -> Result.failure(failure) }
        )
    }
}
