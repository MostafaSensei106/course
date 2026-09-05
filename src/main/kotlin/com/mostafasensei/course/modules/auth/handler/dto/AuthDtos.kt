package com.mostafasensei.course.modules.auth.handler.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequestDto(
    @field:Schema(example = "student@example.com")
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Schema(example = "Secret123!")
    @field:NotBlank(message = "Password is required")
    val password: String
)

data class LoginResponseDto(
    @field:Schema(description = "JWT access token for authenticated requests")
    val accessToken: String,
    @field:Schema(description = "Long-lived JWT refresh token")
    val refreshToken: String,
    @field:Schema(example = "3600", description = "Access token lifetime in seconds")
    val expiresInSeconds: Long,
    @field:Schema(description = "Authenticated user profile")
    val user: UserResponseDto
)

data class UpdateProfileRequestDto(
    @field:Schema(example = "Mostafa")
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:Schema(example = "Mahmoud")
    @field:NotBlank(message = "Last name is required")
    val lastName: String
)

data class RequestPasswordResetDto(
    @field:Schema(example = "student@example.com")
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String
)

data class ResetPasswordRequestDto(
    @field:Schema(description = "Reset token received from the request-reset step")
    @field:NotBlank(message = "Token is required")
    val token: String,

    @field:Schema(example = "NewSecret123!", description = "Must be at least 8 characters")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:NotBlank(message = "New password is required")
    val newPassword: String
)

data class UpdateUserStatusDto(
    @field:Schema(example = "false", description = "Set false to deactivate the account")
    val isActive: Boolean? = null,
    @field:Schema(example = "true", description = "Set true to mark the email as verified")
    val isVerified: Boolean? = null
)
