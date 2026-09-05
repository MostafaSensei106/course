package com.mostafasensei.course.modules.auth.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.delivery.toCode
import com.mostafasensei.course.core.delivery.toHttpStatus
import com.mostafasensei.course.core.security.CurrentUser
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.modules.auth.domain.entity.UserRole
import com.mostafasensei.course.modules.auth.handler.dto.LoginRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.LoginResponseDto
import com.mostafasensei.course.modules.auth.handler.dto.RegisterInstructorRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.RegisterStudentRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.RequestPasswordResetDto
import com.mostafasensei.course.modules.auth.handler.dto.ResetPasswordRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.UpdateProfileRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.UserResponseDto
import com.mostafasensei.course.modules.auth.handler.usecase.GetUserProfileUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.LoginParms
import com.mostafasensei.course.modules.auth.handler.usecase.LoginUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.RegisterUserParams
import com.mostafasensei.course.modules.auth.handler.usecase.RegisterUserUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.RequestPasswordResetUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.ResetPasswordParams
import com.mostafasensei.course.modules.auth.handler.usecase.ResetPasswordUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.UpdateProfileParams
import com.mostafasensei.course.modules.auth.handler.usecase.UpdateUserProfileUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private fun toUserDto(u: com.mostafasensei.course.modules.auth.domain.entity.User) = UserResponseDto(
    id = u.id, email = u.email, firstName = u.firstName, lastName = u.lastName, role = u.role.name
)

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val getProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateUserProfileUseCase,
    private val requestResetUseCase: RequestPasswordResetUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) {

    @PostMapping("/register")
    suspend fun registerStudent(
        @Valid @RequestBody request: RegisterStudentRequestDto
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        return executeRegistration(
            RegisterUserParams(
                email = request.email, password = request.password,
                firstName = request.firstName, lastName = request.lastName,
                role = UserRole.STUDENT
            )
        )
    }

    @PostMapping("/register/instructor")
    suspend fun registerInstructor(
        @Valid @RequestBody request: RegisterInstructorRequestDto
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        return executeRegistration(
            RegisterUserParams(
                email = request.email, password = request.password,
                firstName = request.firstName, lastName = request.lastName,
                role = UserRole.INSTRUCTOR,
                specialization = request.specialization, bio = request.bio
            )
        )
    }

    @PostMapping("/login")
    suspend fun login(@Valid @RequestBody request: LoginRequestDto): ResponseEntity<ApiResponse<LoginResponseDto>> {
        val result = loginUseCase(LoginParms(request.email, request.password))
        return result.fold(
            onSuccess = { (user, tokens) ->
                Responser.ok(LoginResponseDto(tokens.accessToken, tokens.refreshToken, tokens.expiresInSeconds, toUserDto(user)))
            },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LOGIN_FAILED"), f.message) }
        )
    }

    @GetMapping("/me")
    suspend fun me(): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = getProfileUseCase(CurrentUser.requireId())
        return result.fold(
            onSuccess = { Responser.ok(toUserDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("PROFILE_FAILED"), f.message) }
        )
    }

    @PatchMapping("/me")
    suspend fun updateMe(@Valid @RequestBody req: UpdateProfileRequestDto): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = updateProfileUseCase(UpdateProfileParams(CurrentUser.requireId(), req.firstName, req.lastName))
        return result.fold(
            onSuccess = { Responser.ok(toUserDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_PROFILE_FAILED"), f.message) }
        )
    }

    @PostMapping("/password/request-reset")
    suspend fun requestReset(@Valid @RequestBody req: RequestPasswordResetDto): ResponseEntity<ApiResponse<Map<String, String>>> {
        val result = requestResetUseCase(req.email)
        return result.fold(
            onSuccess = { Responser.ok(mapOf("message" to "If the email exists, a reset token was created")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("RESET_REQUEST_FAILED"), f.message) }
        )
    }

    @PostMapping("/password/reset")
    suspend fun reset(@Valid @RequestBody req: ResetPasswordRequestDto): ResponseEntity<ApiResponse<Map<String, String>>> {
        val result = resetPasswordUseCase(ResetPasswordParams(req.token, req.newPassword))
        return result.fold(
            onSuccess = { Responser.ok(mapOf("message" to "Password reset successfully")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("RESET_FAILED"), f.message) }
        )
    }

    private suspend fun executeRegistration(params: RegisterUserParams): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = registerUserUseCase(params)
        return result.fold(
            onSuccess = { Responser.created(toUserDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("REGISTRATION_FAILED"), f.message) }
        )
    }
}
