package com.mostafasensei.course.modules.auth.handler

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Result
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.core.utils.usecase.UseCaseBase
import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.entity.User
import com.mostafasensei.course.modules.auth.domain.entity.UserRole
import com.mostafasensei.course.modules.auth.domain.security.PasswordHasher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*
import kotlin.time.Clock


data class RegisterUserParams(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
)

@Service
class RegisterUserUseCase(
    private val repo: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val clock: Clock,
) : UseCaseBase<User, RegisterUserParams> {

    override suspend fun invoke(params: RegisterUserParams): Result<User, Failures> {
        val existResult = repo.existsByEmail(params.email)
        existResult.fold(onSuccess = { it }, onFailure = {
            Failures.PostgresSqlFailure(
                code = HttpStatus.CONFLICT.name,
                message = "User with email ${params.email} already exists",
            )
        }
        )
        ))


        val now = Clock.System.now()
        val newUser = User(
            id = UUID.randomUUID(),
            email = params.email,
            passwordHash = passwordHasher.hash(params.password),
            firstName = params.firstName,
            lastName = params.lastName,
            role = params.role,
            isVerified = false,
            isActive = true,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
        repo.save(newUser)
    }
}