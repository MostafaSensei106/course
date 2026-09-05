package com.mostafasensei.course.modules.auth.handler.usecase

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Result
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
    val  token: AuthTokens
)

@Service
class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: TokenProvider,
): UseCaseBase<LoginResponse, LoginParms> {
    
    override suspend fun invoke(params: LoginParms): Result<LoginResponse, Failures> {
        TODO("Not yet implemented")
    }
}