package com.mostafasensei.course.modules.auth.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.modules.auth.domain.entity.UserRole
import com.mostafasensei.course.modules.auth.handler.dto.RegisterInstructorRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.RegisterStudentRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.UserResponseDto
import com.mostafasensei.course.modules.auth.handler.usecase.RegisterUserParams
import com.mostafasensei.course.modules.auth.handler.usecase.RegisterUserUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase
) {

    @PostMapping("/register")
    suspend fun registerStudent(
        @Valid @RequestBody request: RegisterStudentRequestDto
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val params = RegisterUserParams(
            email = request.email,
            password = request.password,
            firstName = request.firstName,
            lastName = request.lastName,
            role = UserRole.STUDENT
        )

        return executeRegistration(params)
    }

    @PostMapping("/register/instructor")
    suspend fun registerInstructor(
        @Valid @RequestBody request: RegisterInstructorRequestDto
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val params = RegisterUserParams(
            email = request.email,
            password = request.password,
            firstName = request.firstName,
            lastName = request.lastName,
            role = UserRole.INSTRUCTOR,
            specialization = request.specialization,
            bio = request.bio
        )

        return executeRegistration(params)
    }

    private suspend fun executeRegistration(params: RegisterUserParams): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = registerUserUseCase(params)

        return result.fold(
            onSuccess = { user ->
                val responseDto = UserResponseDto(
                    id = user.id,
                    email = user.email,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    role = user.role.name
                )
                Responser.created(responseDto)
            },
            onFailure = { failure ->
                val status = when (failure) {
                    is Failures.LocalStorageFailure.DuplicateEntryFailure -> HttpStatus.CONFLICT
                    is Failures.LocalStorageFailure.InvalidDataFailure -> HttpStatus.BAD_REQUEST
                    else -> HttpStatus.INTERNAL_SERVER_ERROR
                }
                Responser.error(
                    status = status,
                    code = failure.code ?: "REGISTRATION_FAILED",
                    message = failure.message
                )
            }
        )
    }
}