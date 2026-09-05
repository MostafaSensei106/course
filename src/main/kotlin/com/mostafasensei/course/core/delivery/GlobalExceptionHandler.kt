package com.mostafasensei.course.core.delivery

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { "Validation failed" }
        return Responser.error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ApiResponse<Nothing>> {
        return Responser.error(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", ex.message ?: "Unauthenticated")
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ApiResponse<Nothing>> {
        return Responser.error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.message ?: "Not found")
    }
}
