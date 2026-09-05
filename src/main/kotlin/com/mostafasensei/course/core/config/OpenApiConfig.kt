package com.mostafasensei.course.core.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger / OpenAPI documentation setup (springdoc-openapi).
 *
 * - Interactive UI: `http://localhost:8080/swagger-ui/index.html`
 * - Raw OpenAPI JSON (Postman import): `http://localhost:8080/v3/api-docs`
 * - The global `BearerAuth` requirement enables the **Authorize** button:
 *   paste a JWT access token (without the `Bearer ` prefix) to try out
 *   secured endpoints straight from the browser.
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "BearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("Barmagy Course LMS API")
                    .version("1.0.0")
                    .description("RESTful API documentation for Course Platform built with Clean Architecture, Spring Boot, and Kotlin.")
                    .contact(
                        Contact()
                            .name("Mostafa Sensei")
                            .email("mostafasensei106@gmail.com")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter your JWT access token here (without 'Bearer ' prefix)")
                )
            )
    }
}
