package com.mostafasensei.course.core.security

import com.mostafasensei.course.core.router.ApiRoutes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // public auth endpoints
                    .requestMatchers("${ApiRoutes.Auth.BASE}/**").permitAll()
                    // public catalog reads
                    .requestMatchers(HttpMethod.GET, "${ApiRoutes.Categories.BASE}/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "${ApiRoutes.Courses.BASE}/**").permitAll()
                    // actuator + errors
                    .requestMatchers("/actuator/**", "/error").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
