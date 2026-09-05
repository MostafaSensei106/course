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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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

@Tag(name = "Admin - Users", description = "Admin endpoints for managing users")
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

    @Operation(summary = "Create privileged user", description = "Creates a user with an explicit role (SUPER_ADMIN only)")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "User created"),
        SwaggerApiResponse(responseCode = "409", description = "Email already registered"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        SwaggerApiResponse(responseCode = "403", description = "Insufficient privileges")
    )
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

    @Operation(summary = "List users", description = "Returns a paginated list of users")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Users returned"),
        SwaggerApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    )
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

    @Operation(summary = "Get user by id", description = "Returns a single user by UUID")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "User returned"),
        SwaggerApiResponse(responseCode = "404", description = "User not found")
    )
    @GetMapping(ApiRoutes.AdminUsers.BY_ID)
    suspend fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = getUserByIdUseCase(id)
        return result.fold(
            onSuccess = { Responser.ok(toDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("GET_USER_FAILED"), f.message) }
        )
    }

    @Operation(summary = "Update user status", description = "Activates/deactivates or verifies a user account")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "User updated"),
        SwaggerApiResponse(responseCode = "404", description = "User not found")
    )
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

    @Operation(summary = "Delete user", description = "Deletes a user permanently (SUPER_ADMIN only)")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "User deleted"),
        SwaggerApiResponse(responseCode = "404", description = "User not found"),
        SwaggerApiResponse(responseCode = "403", description = "Insufficient privileges")
    )
    @DeleteMapping(ApiRoutes.AdminUsers.BY_ID)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    suspend fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Map<String, String>>> {
        val result = deleteUserUseCase(id)
        return result.fold(
            onSuccess = { Responser.ok(mapOf("message" to "User deleted")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DELETE_USER_FAILED"), f.message) }
        )
    }

    @Operation(summary = "Deactivate user", description = "Deactivates a user account without deleting it")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "User deactivated"),
        SwaggerApiResponse(responseCode = "404", description = "User not found")
    )
    @PostMapping(ApiRoutes.AdminUsers.DEACTIVATE)
    suspend fun deactivate(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponseDto>> {
        val result = deactivateUserUseCase(id)
        return result.fold(
            onSuccess = { Responser.ok(toDto(it)) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DEACTIVATE_USER_FAILED"), f.message) }
        )
    }
}
