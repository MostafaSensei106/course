package com.mostafasensei.course.modules.auth.domain.security

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(raw: String, hashed: String): Boolean
}