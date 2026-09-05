package com.mostafasensei.course.core.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String = "change-me-to-a-very-long-secret-key-for-hs256-min-32-bytes-please!!",
    val accessExpirationSeconds: Long = 3600,
    val refreshExpirationSeconds: Long = 604800
)
