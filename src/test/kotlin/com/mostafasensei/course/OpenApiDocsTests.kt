package com.mostafasensei.course

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Proves springdoc serves the OpenAPI document and that it picked up
 * the real controllers (incl. suspend functions) and the JWT scheme.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `api-docs exposes documented paths and JWT scheme`() {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.info.title").value("Barmagy Course LMS API"))
            .andExpect(jsonPath("$.paths./api/v1/auth/login.post").exists())
            .andExpect(jsonPath("$.paths./api/v1/auth/register.post").exists())
            .andExpect(jsonPath("$.paths./api/v1/courses.get").exists())
            .andExpect(jsonPath("$.paths./api/v1/admin/users.get").exists())
            .andExpect(jsonPath("$.components.securitySchemes.BearerAuth").exists())
    }
}
