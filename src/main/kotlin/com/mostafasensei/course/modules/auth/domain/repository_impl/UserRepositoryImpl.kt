package com.mostafasensei.course.modules.auth.domain.repository_impl

import com.mostafasensei.course.core.utils.result.Executor
import com.mostafasensei.course.core.utils.result.StorageResult
import com.mostafasensei.course.modules.auth.data.models.UserJpaEntity
import com.mostafasensei.course.modules.auth.data.repository.SpringJpaUserRepository
import com.mostafasensei.course.modules.auth.data.repository.UserRepository
import com.mostafasensei.course.modules.auth.domain.entity.User
import org.springframework.stereotype.Repository


@Repository
class UserRepositoryImpl(
    private val jpaRepo: SpringJpaUserRepository
) : UserRepository {
    override fun findByUsername(username: String): StorageResult<User> = findByEmail(username)

    override fun findByEmail(email: String): StorageResult<User> = Executor.execute {
        val e = jpaRepo.findByEmail(email).orElseThrow{
            NoSuchElementException("User not found with email: $email")
        }
        e.toDomain()
    }

    override fun existsByEmail(email: String): StorageResult<Boolean> = Executor.execute {
        jpaRepo.existsByEmail(email)
    }

    override fun save(user: User): StorageResult<User> = Executor.execute {
        val  e= UserJpaEntity.fromDomain(user)
        jpaRepo.save(e).toDomain()
    }

}