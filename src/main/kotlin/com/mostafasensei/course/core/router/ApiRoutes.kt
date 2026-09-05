package com.mostafasensei.course.core.router

/**
 * Single source of truth for every HTTP route in the application.
 *
 * All values are `const val` so they can be used directly in Spring
 * mapping annotations (`@RequestMapping`, `@GetMapping`, ...).
 * Change a path here once and every controller + [SecurityConfig][com.mostafasensei.course.core.security.SecurityConfig]
 * picks it up automatically.
 */
object ApiRoutes {

    const val API_V1 = "/api/v1"

    object Auth {
        const val BASE = "$API_V1/auth"
        const val REGISTER = "/register"
        const val REGISTER_INSTRUCTOR = "/register/instructor"
        const val LOGIN = "/login"
        const val ME = "/me"
        const val PASSWORD_REQUEST_RESET = "/password/request-reset"
        const val PASSWORD_RESET = "/password/reset"
    }

    object AdminUsers {
        const val BASE = "$API_V1/admin/users"
        const val BY_ID = "/{id}"
        const val DEACTIVATE = "/{id}/deactivate"
    }

    object Categories {
        const val BASE = "$API_V1/categories"
        const val BY_SLUG_OR_ID = "/{slugOrId}"
    }

    object AdminCategories {
        const val BASE = "$API_V1/admin/categories"
        const val BY_ID = "/{id}"
    }

    object Courses {
        const val BASE = "$API_V1/courses"
        const val BY_SLUG_OR_ID = "/{slugOrId}"
        const val BY_ID = "/id/{id}"
    }

    object AdminCourses {
        const val BASE = "$API_V1/admin/courses"
        const val BY_ID = "/{id}"
        const val PUBLISH = "/{id}/publish"
    }

    object AdminSections {
        const val BASE = "$API_V1/admin/courses/{courseId}/sections"
        const val BY_ID = "/{sectionId}"
    }

    object AdminLessons {
        const val BASE = "$API_V1/admin/sections/{sectionId}/lessons"
        const val BY_ID = "/{lessonId}"
    }

    object Enrollments {
        const val BASE = "$API_V1/enrollments"
        const val ME = "/me"
        const val BY_ID = "/{id}"
        const val PROGRESS = "/{id}/progress"
    }
}
