package com.mostafasensei.course.core.router

import com.mostafasensei.course.core.logging.appLogger
import com.mostafasensei.course.core.logging.infoEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * Prints the full route table once at startup (method + path + handler)
 * so the effective router state is always visible in the boot log.
 *
 * Disable with `app.router.log-routes=false`.
 */
@Component
class RouteTableLogger(
    @Qualifier("requestMappingHandlerMapping") private val handlerMapping: RequestMappingHandlerMapping,
    @Value("\${app.router.log-routes:true}") private val enabled: Boolean,
) : ApplicationRunner {

    private val log = appLogger<RouteTableLogger>()

    override fun run(args: ApplicationArguments) {
        if (!enabled) return
        handlerMapping.handlerMethods
            .flatMap { (info, method) ->
                val httpMethods = info.methodsCondition.methods
                    .map { it.name }
                    .ifEmpty { listOf("ALL") }
                    .joinToString(",")
                val paths = info.pathPatternsCondition?.patternValues.orEmpty()
                paths.map { Triple(httpMethods, it, "${method.beanType.simpleName}#${method.method.name}") }
            }
            .sortedWith(compareBy({ it.second }, { it.first }))
            .forEach { (httpMethod, path, handler) ->
                log.infoEvent("route", "method" to httpMethod, "path" to path, "handler" to handler)
            }
    }
}
