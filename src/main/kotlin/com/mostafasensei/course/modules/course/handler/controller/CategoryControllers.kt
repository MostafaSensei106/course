package com.mostafasensei.course.modules.course.handler.controller

import com.mostafasensei.course.core.delivery.ApiResponse
import com.mostafasensei.course.core.delivery.Responser
import com.mostafasensei.course.core.delivery.toCode
import com.mostafasensei.course.core.delivery.toHttpStatus
import com.mostafasensei.course.core.router.ApiRoutes
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.modules.course.handler.dto.*
import com.mostafasensei.course.modules.course.handler.usecase.*
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ---------------- Public category reads ----------------
@Tag(name = "Categories", description = "Public category browsing")
@RestController
@RequestMapping(ApiRoutes.Categories.BASE)
class CategoryController(
    private val listCategoriesUseCase: ListCategoriesUseCase,
    private val getBySlugUseCase: GetCategoryBySlugUseCase,
    private val getByIdUseCase: GetCategoryUseCase
) {
    @Operation(summary = "List categories", description = "Returns all course categories")
    @ApiResponses(SwaggerApiResponse(responseCode = "200", description = "Categories returned"))
    @GetMapping
    suspend fun list(): ResponseEntity<ApiResponse<List<CategoryResponse>>> =
        listCategoriesUseCase(Unit).fold(
            onSuccess = { Responser.ok(it.map { c -> c.toResponse() }) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_CATEGORIES_FAILED"), f.message) }
        )

    @Operation(summary = "Get category", description = "Returns a single category by slug or UUID")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Category returned"),
        SwaggerApiResponse(responseCode = "404", description = "Category not found")
    )
    @GetMapping(ApiRoutes.Categories.BY_SLUG_OR_ID)
    suspend fun get(@PathVariable slugOrId: String): ResponseEntity<ApiResponse<CategoryResponse>> {
        val parsed = runCatching { UUID.fromString(slugOrId) }.getOrNull()
        val result = if (parsed != null) getByIdUseCase(parsed) else getBySlugUseCase(slugOrId)
        return result.fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("GET_CATEGORY_FAILED"), f.message) }
        )
    }
}

// ---------------- Admin category CRUD ----------------
@Tag(name = "Admin - Categories", description = "Admin endpoints for managing categories")
@RestController
@RequestMapping(ApiRoutes.AdminCategories.BASE)
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MIN_ADMIN')")
class AdminCategoryController(
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val listCategoriesUseCase: ListCategoriesUseCase
) {
    @Operation(summary = "Create category", description = "Creates a new course category")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "Category created"),
        SwaggerApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        SwaggerApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    )
    @PostMapping
    suspend fun create(@Valid @RequestBody req: CreateCategoryRequest): ResponseEntity<ApiResponse<CategoryResponse>> =
        createCategoryUseCase(CreateCategoryParams(req.name, req.description)).fold(
            onSuccess = { Responser.created(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("CREATE_CATEGORY_FAILED"), f.message) }
        )

    @Operation(summary = "Update category", description = "Updates name or description of a category")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Category updated"),
        SwaggerApiResponse(responseCode = "404", description = "Category not found")
    )
    @PutMapping(ApiRoutes.AdminCategories.BY_ID)
    suspend fun update(@PathVariable id: UUID, @RequestBody req: UpdateCategoryRequest): ResponseEntity<ApiResponse<CategoryResponse>> =
        updateCategoryUseCase(UpdateCategoryParams(id, req.name, req.description)).fold(
            onSuccess = { Responser.ok(it.toResponse()) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("UPDATE_CATEGORY_FAILED"), f.message) }
        )

    @Operation(summary = "Delete category", description = "Deletes a category permanently")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "Category deleted"),
        SwaggerApiResponse(responseCode = "404", description = "Category not found")
    )
    @DeleteMapping(ApiRoutes.AdminCategories.BY_ID)
    suspend fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Map<String, String>>> =
        deleteCategoryUseCase(id).fold(
            onSuccess = { Responser.ok(mapOf("message" to "Category deleted")) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("DELETE_CATEGORY_FAILED"), f.message) }
        )

    @Operation(summary = "List all categories (admin)", description = "Returns all categories including unpublished ones")
    @ApiResponses(SwaggerApiResponse(responseCode = "200", description = "Categories returned"))
    @GetMapping
    suspend fun listAll(): ResponseEntity<ApiResponse<List<CategoryResponse>>> =
        listCategoriesUseCase(Unit).fold(
            onSuccess = { Responser.ok(it.map { c -> c.toResponse() }) },
            onFailure = { f -> Responser.error(f.toHttpStatus(), f.toCode("LIST_CATEGORIES_FAILED"), f.message) }
        )
}
