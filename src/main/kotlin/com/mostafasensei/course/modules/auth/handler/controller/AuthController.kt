package com.mostafasensei.course.modules.auth.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.delivery.toCode
import com.mostafasensei.course.core.delivery.toHttpStatus
import com.mostafasensei.course.core.router.ApiRoutes
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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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

@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, and session handling")
@RestController
@RequestMapping(ApiRoutes.Auth.BASE)
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val getProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateUserProfileUseCase,
    private val requestResetUseCase: RequestPasswordResetUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) {

    @Operation(summary = "Register a new student", description = "Creates a new public student account")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "Student successfully registered"),
        SwaggerApiResponse(responseCode = "409", description = "Email already registered"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error")
    )
    @PostMapping(ApiRoutes.Auth.REGISTER)
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

    @Operation(summary = "Register a new instructor", description = "Creates a new instructor account with specialization")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "Instructor successfully registered"),
        SwaggerApiResponse(responseCode = "409", description = "Email already registered"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error")
    )
    @PostMapping(ApiRoutes.Auth.REGISTER_INSTRUCTOR)
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

    @Operation(summary = "Login to account", description = "Authenticates user and returns JWT access and refresh tokens")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Successfully logged in"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid credentials or account deactivated")
    )
    @PostMapping(ApiRoutes.Auth.LOGIN)
    suspend fun login(@Valid @RequestBody request: LoginRequestDto): ResponseEntity<ApiResponse<LoginResponseDto>> {
        val result = loginUseCase(LoginParms(request.email, request.password))
        return result.fold(
            onSuccess = { (user, tokens) ->
                Responser.ok(LoginResponseDto(tokens.accessToken, tokens.refreshToken, tokens.expiresInSeconds, toUserDto(user)))
            },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LOGIN_FAILED"), f.message) }
        )
    }

    @Operation(summary = "Get current profile", description = "Returns the profile of the authenticated user")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Profile returned"),
        SwaggerApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
        SwaggerApiResponse(responseCode = "404", description = "User not found")
    )
    @GetMapping(ApiRoutes.Auth.ME)
    suspend fun me(): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = getProfileUseCase(CurrentUser.requireId())
        return result.fold(
            onSuccess = { Responser.ok(toUserDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("PROFILE_FAILED"), f.message) }
        )
    }

    @Operation(summary = "Update current profile", description = "Updates first and last name of the authenticated user")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Profile updated"),
        SwaggerApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error")
    )
    @PatchMapping(ApiRoutes.Auth.ME)
    suspend fun updateMe(@Valid @RequestBody req: UpdateProfileRequestDto): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = updateProfileUseCase(UpdateProfileParams(CurrentUser.requireId(), req.firstName, req.lastName))
        return result.fold(
            onSuccess = { Responser.ok(toUserDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_PROFILE_FAILED"), f.message) }
        )
    }

    @Operation(summary = "Request password reset", description = "Creates a password-reset token for the given email")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Reset token created (if the email exists)"),
        SwaggerApiResponse(responseCode = "404", description = "Email not registered")
    )
    @PostMapping(ApiRoutes.Auth.PASSWORD_REQUEST_RESET)
    suspend fun requestReset(@Valid @RequestBody req: RequestPasswordResetDto): ResponseEntity<ApiResponse<Map<String, String>>> {
        val result = requestResetUseCase(req.email)
        return result.fold(
            onSuccess = { Responser.ok(mapOf("message" to "If the email exists, a reset token was created")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("RESET_REQUEST_FAILED"), f.message) }
        )
    }

    @Operation(summary = "Reset password", description = "Sets a new password using a valid reset token")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Password reset successfully"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        SwaggerApiResponse(responseCode = "404", description = "Invalid or expired reset token")
    )
    @PostMapping(ApiRoutes.Auth.PASSWORD_RESET)
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
