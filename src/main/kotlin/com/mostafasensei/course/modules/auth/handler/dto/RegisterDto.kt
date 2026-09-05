package com.mostafasensei.course.modules.auth.handler.dto

import com.mostafasensei.course.modules.auth.domain.entity.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterStudentRequestDto(
    @field:Schema(example = "student@example.com", description = "Unique email address")
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Schema(example = "Secret123!", description = "Must be at least 8 characters")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:Schema(example = "Mostafa")
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:Schema(example = "Mahmoud")
    @field:NotBlank(message = "Last name is required")
    val lastName: String
)

data class RegisterInstructorRequestDto(
    @field:Schema(example = "instructor@example.com", description = "Unique email address")
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Schema(example = "Secret123!", description = "Must be at least 8 characters")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:Schema(example = "Mostafa")
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:Schema(example = "Mahmoud")
    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:Schema(example = "Mathematics", description = "Instructor field of expertise")
    @field:NotBlank(message = "Specialization is required for instructors")
    val specialization: String,

    @field:Schema(example = "10 years teaching algebra and calculus")
    val bio: String? = null
)

data class CreatePrivilegedUserRequestDto(
    @field:Schema(example = "admin@example.com", description = "Unique email address")
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Schema(example = "Secret123!", description = "Must be at least 8 characters")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:Schema(example = "Mostafa")
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:Schema(example = "Mahmoud")
    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:Schema(example = "ADMIN", description = "Explicit role for the new user")
    @field:NotNull(message = "Role must be explicitly provided")
    val role: UserRole
)

data class UserResponseDto(
    @field:Schema(description = "User UUID")
    val id: UUID,
    @field:Schema(example = "student@example.com")
    val email: String,
    @field:Schema(example = "Mostafa")
    val firstName: String,
    @field:Schema(example = "Mahmoud")
    val lastName: String,
    @field:Schema(example = "STUDENT", description = "One of GUST, STUDENT, STUDENT_SUPERVISOR, INSTRUCTOR, MIN_ADMIN, ADMIN, SUPER_ADMIN")
    val role: String
)
