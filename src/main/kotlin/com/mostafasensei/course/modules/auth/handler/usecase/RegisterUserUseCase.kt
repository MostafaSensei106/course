package com.mostafasensei.course.modules.auth.handler.usecase

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Result
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.core.utils.usecase.UseCaseBase
import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.entity.User
import com.mostafasensei.course.modules.auth.domain.entity.UserRole
import com.mostafasensei.course.modules.auth.domain.security.PasswordHasher
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID


data class RegisterUserParams(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val specialization: String? = null,
    val bio: String? = null
)

@Service
class RegisterUserUseCase(
    private val repo: UserRepository,
    private val passwordHasher: PasswordHasher,
) : UseCaseBase<User, RegisterUserParams> {

    override suspend fun invoke(params: RegisterUserParams): Result<User, Failures> {
        val existResult = repo.existsByEmail(params.email)
        val exists = existResult.fold(
            onSuccess = { it },
            onFailure = { failure -> return Result.failure(failure) },
        )
        if (exists) {
            return Result.failure(
                Failures.LocalStorageFailure.DuplicateEntryFailure(
                    message = "Email already registered",
                    code = "EMAIL_ALREADY_EXISTS"
                )
            )
        }

        val now = Instant.now()
        val newUser = User(
            id = UUID.randomUUID(),
            email = params.email,
            passwordHash = requireNotNull(passwordHasher.hash(params.password)) { "Password hashing failed" },
            firstName = params.firstName,
            lastName = params.lastName,
            role = params.role,
            isVerified = false,
            isActive = true,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
        return repo.save(newUser)
    }
}
