package com.mostafasensei.course.core.delivery

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorInfo(val code: String? = null , val message: String? = null)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Meta(
    val page: Int? = null,
    @JsonProperty("per_size") val pageSize: Int? = null,
    @JsonProperty("total_pages") val totalPages: Int? = null,
    val next: Int? = null,
    val prev: Int? = null, )

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    @JsonProperty("errors") val errors: ErrorInfo? = null,
    val mate: Meta? = null,
)



class Responser<T> private constructor() {
    private var status: HttpStatus = HttpStatus.OK
    private var data: T? = null
    private var error: ErrorInfo? = null
    private var meta: Meta? = null
    private var success: Boolean = true


    fun status(status: HttpStatus) = apply{
        this.status = status
    }

    fun status(statusCode: Int) = apply {
        this.status = HttpStatus.valueOf(statusCode)
    }

    fun withData(data: T) = apply {this.data = data
    this.success = true
    }

    fun withMeta(mate: Meta) = apply {this.meta = mate}

    fun withError(code: String, message: String) = apply {
        this.success = false
        this.error = ErrorInfo(code, message)
    }

    fun send(): ResponseEntity<ApiResponse<T>> {
        val body = ApiResponse(
            success = this.success,
            data = this.data,
            errors = this.error,
            mate = this.meta,
        )
        return ResponseEntity.status(status).body(body)
    }

    companion object {
        /// Ok
        fun <T> ok(data: T): ResponseEntity<ApiResponse<T>> = Responser<T>().withData(data).status(HttpStatus.OK).send()
        /// Created
        fun <T> created(data: T): ResponseEntity<ApiResponse<T>> = Responser<T>().withData(data).status(HttpStatus.CREATED).send()
        /// Error
        fun <T> error (status: HttpStatus, code: String, message: String): ResponseEntity<ApiResponse<T>> = Responser<T>().withError(code, message).status(status).send()

        fun <T> sender(): Responser<T> = Responser()
    }
}