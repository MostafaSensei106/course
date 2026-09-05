package com.mostafasensei.course

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("com.mostafasensei.course.core.security")
class CourseApplication

fun main(args: Array<String>) {
    runApplication<CourseApplication>(*args)
}
