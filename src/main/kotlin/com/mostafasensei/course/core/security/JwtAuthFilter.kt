package com.mostafasensei.course.core.security

import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.security.JwtTokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            val userId = jwtTokenProvider.extractUserId(token)
            if (userId != null && SecurityContextHolder.getContext().authentication == null) {
                runBlocking {
                    val result = userRepository.findById(java.util.UUID.fromString(userId))
                    val user = result.getOrNull()
                    if (user != null && user.isActive && jwtTokenProvider.isValid(token)) {
                        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                        val auth = UsernamePasswordAuthenticationToken(user, null, authorities)
                        auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = auth
                    }
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}
