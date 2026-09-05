package com.mostafasensei.course.modules.auth.handler.dto

import com.mostafasensei.course.modules.auth.domain.entity.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterStudentRequestDto(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String
)

data class RegisterInstructorRequestDto(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:NotBlank(message = "Specialization is required for instructors")
    val specialization: String,

    val bio: String? = null
)

data class CreatePrivilegedUserRequestDto(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:NotNull(message = "Role must be explicitly provided")
    val role: UserRole
)

data class UserResponseDto(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String
)