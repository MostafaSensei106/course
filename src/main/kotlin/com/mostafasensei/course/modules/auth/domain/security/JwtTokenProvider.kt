package com.mostafasensei.course.modules.auth.domain.security

import com.mostafasensei.course.core.security.JwtProperties
import com.mostafasensei.course.modules.auth.domain.entity.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProvider(
    private val props: JwtProperties
) : TokenProvider {

    private fun key() = Keys.hmacShaKeyFor(props.secret.toByteArray())

    override fun generateTokens(user: User): AuthTokens {
        val now = System.currentTimeMillis()
        val accessExp = Date(now + props.accessExpirationSeconds * 1000)
        val refreshExp = Date(now + props.refreshExpirationSeconds * 1000)

        val accessToken = Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.email)
            .claim("role", user.role.name)
            .issuedAt(Date(now))
            .expiration(accessExp)
            .signWith(key())
            .compact()

        val refreshToken = Jwts.builder()
            .subject(user.id.toString())
            .claim("type", "refresh")
            .issuedAt(Date(now))
            .expiration(refreshExp)
            .signWith(key())
            .compact()

        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInSeconds = props.accessExpirationSeconds
        )
    }

    override fun extractUserId(token: String): String? = try {
        Jwts.parser()
            .verifyWith(key())
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    } catch (e: Exception) {
        null
    }

    fun extractRole(token: String): String? = try {
        Jwts.parser()
            .verifyWith(key())
            .build()
            .parseSignedClaims(token)
            .payload
            .get("role", String::class.java)
    } catch (e: Exception) {
        null
    }

    fun isValid(token: String): Boolean = try {
        Jwts.parser().verifyWith(key()).build().parseSignedClaims(token)
        true
    } catch (e: Exception) {
        false
    }
}
