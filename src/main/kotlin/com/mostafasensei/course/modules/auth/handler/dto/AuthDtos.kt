package com.mostafasensei.course.modules.auth.handler.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequestDto(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val user: UserResponseDto
)

data class UpdateProfileRequestDto(
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String
)

data class RequestPasswordResetDto(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String
)

data class ResetPasswordRequestDto(
    @field:NotBlank(message = "Token is required")
    val token: String,

    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:NotBlank(message = "New password is required")
    val newPassword: String
)

data class UpdateUserStatusDto(
    val isActive: Boolean? = null,
    val isVerified: Boolean? = null
)
