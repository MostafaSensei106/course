package com.mostafasensei.course.modules.auth.domain.security

import com.mostafasensei.course.modules.auth.domain.entity.User

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long
)

interface TokenProvider {
    fun generateTokens(user: User): AuthTokens
    fun extractUserId(token: String): String?
}