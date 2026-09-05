package com.mostafasensei.course.modules.auth.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.modules.auth.handler.dto.CreatePrivilegedUserRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.UserResponseDto
import com.mostafasensei.course.modules.auth.handler.usecase.RegisterUserParams
import com.mostafasensei.course.modules.auth.handler.usecase.RegisterUserUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminUserController(
    private val registerUserUseCase: RegisterUserUseCase
) {

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    suspend fun createPrivilegedUser(
        @Valid @RequestBody request: CreatePrivilegedUserRequestDto
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val params = RegisterUserParams(
            email = request.email,
            password = request.password,
            firstName = request.firstName,
            lastName = request.lastName,
            role = request.role
        )

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
                    code = failure.code ?: "USER_CREATION_FAILED",
                    message = failure.message
                )
            }
        )
    }
}