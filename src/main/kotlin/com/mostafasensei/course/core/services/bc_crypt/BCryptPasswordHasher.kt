package com.mostafasensei.course.core.services.bc_crypt

import com.mostafasensei.course.modules.auth.domain.security.PasswordHasher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasher: PasswordHasher {
    private val encoder = BCryptPasswordEncoder()
    override fun hash(raw: String): String = encoder.encode(raw)
    override fun verify(raw: String,hashed:String ): Boolean = encoder.matches(raw, hashed)
}