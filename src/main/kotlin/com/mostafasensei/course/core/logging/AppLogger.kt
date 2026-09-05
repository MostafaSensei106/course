package com.mostafasensei.course.core.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Logger factory + tiny structured-event helpers.
 *
 * Usage:
 * ```
 * private val log = appLogger<AuthController>()
 * log.infoEvent("user.login", "email" to email, "role" to role)
 * log.errorEvent("user.login.failed", ex, "email" to email)
 * ```
 * Events render as `message key=value key=value` so plain-text logs stay
 * grep-friendly without pulling in a JSON layout dependency.
 */
inline fun <reified T : Any> appLogger(): Logger = LoggerFactory.getLogger(T::class.java)

private fun render(fields: Array<out Pair<String, Any?>>): String =
    fields.joinToString(" ") { (k, v) -> "$k=${v ?: "-"}" }

fun Logger.infoEvent(event: String, vararg fields: Pair<String, Any?>) {
    if (isInfoEnabled) info("$event ${render(fields)}")
}

fun Logger.warnEvent(event: String, vararg fields: Pair<String, Any?>) {
    if (isWarnEnabled) warn("$event ${render(fields)}")
}

fun Logger.errorEvent(event: String, cause: Throwable? = null, vararg fields: Pair<String, Any?>) {
    if (isErrorEnabled) {
        if (cause != null) error("$event ${render(fields)}", cause)
        else error("$event ${render(fields)}")
    }
}

fun Logger.debugEvent(event: String, vararg fields: Pair<String, Any?>) {
    if (isDebugEnabled) debug("$event ${render(fields)}")
}
