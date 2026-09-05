package com.mostafasensei.course.core.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Outer HTTP logging filter.
 *
 * - Propagates (or generates) an `X-Request-Id` header so a client call can
 *   be traced across every log line via MDC (`%X{requestId}` in the log pattern).
 * - Logs one line per request: `method path -> status in ms (user=...)`.
 * - Runs at highest precedence so the request id is available to every
 *   downstream filter, including the JWT auth filter.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter(
    @Value("\${app.logging.http.enabled:true}") private val enabled: Boolean,
    @Value("\${app.logging.http.skip-paths:/actuator,/favicon.ico}") private val skipPaths: List<String>,
) : OncePerRequestFilter() {

    private val log = appLogger<RequestLoggingFilter>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
        MDC.put(MDC_REQUEST_ID, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        val start = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
        } finally {
            try {
                if (enabled && skipPaths.none { request.requestURI.startsWith(it) }) {
                    val principal = SecurityContextHolder.getContext().authentication
                    val user = principal?.takeIf { it.isAuthenticated }?.name ?: "-"
                    log.infoEvent(
                        "http",
                        "method" to request.method,
                        "path" to request.requestURI,
                        "status" to response.status,
                        "ms" to (System.currentTimeMillis() - start),
                        "user" to user,
                    )
                }
            } finally {
                MDC.clear()
            }
        }
    }

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val MDC_REQUEST_ID = "requestId"
    }
}
