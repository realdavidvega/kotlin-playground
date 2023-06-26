package com.example.r2dbc.users

import org.springframework.stereotype.Service

@Service
class UserService(val repository: CoroutineUserRepository) {
    suspend fun findUserById(id: Long): User? =
        repository.findById(id)?.toDomain()
}
