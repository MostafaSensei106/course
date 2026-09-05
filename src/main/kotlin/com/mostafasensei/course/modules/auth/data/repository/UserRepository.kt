package com.mostafasensei.course.modules.auth.data.repository

import com.mostafasensei.course.core.utils.result.StorageResult
import com.mostafasensei.course.modules.auth.domain.entity.User

interface UserRepository {
    fun findByUsername(username: String): StorageResult<User>
    fun findByEmail(email: String): StorageResult<User>
    fun existsByEmail(email: String): StorageResult<Boolean>
    fun save(user: User): StorageResult<User>
}