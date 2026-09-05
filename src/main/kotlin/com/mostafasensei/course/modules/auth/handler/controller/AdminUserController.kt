package com.mostafasensei.course.modules.auth.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.delivery.toCode
import com.mostafasensei.course.core.delivery.toHttpStatus
import com.mostafasensei.course.core.router.ApiRoutes
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.modules.auth.handler.dto.CreatePrivilegedUserRequestDto
import com.mostafasensei.course.modules.auth.handler.dto.UpdateUserStatusDto
import com.mostafasensei.course.modules.auth.handler.dto.UserResponseDto
import com.mostafasensei.course.modules.auth.handler.usecase.DeactivateUserUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.DeleteUserUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.GetUserByIdUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.ListUsersParams
import com.mostafasensei.course.modules.auth.handler.usecase.ListUsersUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.RegisterUserParams
import com.mostafasensei.course.modules.auth.handler.usecase.RegisterUserUseCase
import com.mostafasensei.course.modules.auth.handler.usecase.UpdateUserStatusParams
import com.mostafasensei.course.modules.auth.handler.usecase.UpdateUserStatusUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

private fun toDto(u: com.mostafasensei.course.modules.auth.domain.entity.User) = UserResponseDto(
    id = u.id, email = u.email, firstName = u.firstName, lastName = u.lastName, role = u.role.name
)

@RestController
@RequestMapping(ApiRoutes.AdminUsers.BASE)
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MIN_ADMIN')")
class AdminUserController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val listUsersUseCase: ListUsersUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateStatusUseCase: UpdateUserStatusUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val deactivateUserUseCase: DeactivateUserUseCase,
) {

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    suspend fun createPrivilegedUser(
        @Valid @RequestBody request: CreatePrivilegedUserRequestDto
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = registerUserUseCase(
            RegisterUserParams(
                email = request.email, password = request.password,
                firstName = request.firstName, lastName = request.lastName, role = request.role
            )
        )
        return result.fold(
            onSuccess = { Responser.created(toDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("USER_CREATION_FAILED"), f.message) }
        )
    }

    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<UserResponseDto>>> {
        val result = listUsersUseCase(ListUsersParams(page, size))
        return result.fold(
            onSuccess = { Responser.ok(it.map(::toDto)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_USERS_FAILED"), f.message) }
        )
    }

    @GetMapping(ApiRoutes.AdminUsers.BY_ID)
    suspend fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = getUserByIdUseCase(id)
        return result.fold(
            onSuccess = { Responser.ok(toDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("GET_USER_FAILED"), f.message) }
        )
    }

    @PatchMapping(ApiRoutes.AdminUsers.BY_ID)
    suspend fun updateStatus(
        @PathVariable id: UUID,
        @RequestBody req: UpdateUserStatusDto
    ): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = updateStatusUseCase(UpdateUserStatusParams(id, req.isActive, req.isVerified))
        return result.fold(
            onSuccess = { Responser.ok(toDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_USER_FAILED"), f.message) }
        )
    }

    @DeleteMapping(ApiRoutes.AdminUsers.BY_ID)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    suspend fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Map<String, String>>> {
        val result = deleteUserUseCase(id)
        return result.fold(
            onSuccess = { Responser.ok(mapOf("message" to "User deleted")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DELETE_USER_FAILED"), f.message) }
        )
    }

    @PostMapping(ApiRoutes.AdminUsers.DEACTIVATE)
    suspend fun deactivate(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = deactivateUserUseCase(id)
        return result.fold(
            onSuccess = { Responser.ok(toDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DEACTIVATE_USER_FAILED"), f.message) }
        )
    }
}
