package com.mostafasensei.course.modules.auth.handler.usecase

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Result
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.core.utils.usecase.UseCaseBase
import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.entity.User
import com.mostafasensei.course.modules.auth.domain.security.AuthTokens
import com.mostafasensei.course.modules.auth.domain.security.PasswordHasher
import com.mostafasensei.course.modules.auth.domain.security.TokenProvider
import org.springframework.stereotype.Service

data class LoginParms(
    val email: String,
    val password: String
)

data class LoginResponse(
    val user: User,
    val token: AuthTokens
)

@Service
class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: TokenProvider,
): UseCaseBase<LoginResponse, LoginParms> {

    override suspend fun invoke(params: LoginParms): Result<LoginResponse, Failures> {
        return userRepository.findByEmail(params.email).fold(
            onFailure = { failure -> Result.failure(failure) },
            onSuccess = { user ->
                if (!user.isActive) {
                    Result.failure(
                    Failures.LocalStorageFailure.InvalidDataFailure(
                        message = "Account is deactivated",
                        code = "ACCOUNT_DEACTIVATED"
                    )
                    )
                } else if (!passwordHasher.verify(params.password, user.passwordHash)) {
                    Result.failure(
                        Failures.LocalStorageFailure.InvalidDataFailure(
                            message = "Invalid email or password",
                            code = "INVALID_CREDENTIALS"
                        )
                    )
                } else {
                    val tokens = tokenProvider.generateTokens(user)
                    Result.success(LoginResponse(user, tokens))
                }
            }
        )
    }
}