package com.mostafasensei.course.core.security

import com.mostafasensei.course.modules.auth.domain.entity.User
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

object CurrentUser {
    fun get(): User? =
        SecurityContextHolder.getContext().authentication?.principal as? User

    fun id(): UUID? = get()?.id

    fun requireId(): UUID =
        id() ?: throw IllegalStateException("Unauthenticated: no current user")
}
