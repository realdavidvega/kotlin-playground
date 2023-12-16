package com.example.r2dbc.user.domain

import com.example.r2dbc.user.infrastructure.CoroutineUserRepository
import com.example.r2dbc.user.infrastructure.UserDTO
import org.springframework.stereotype.Service

interface UserService {
  data class User(val username: String, val email: String)

  suspend fun findUserById(id: Long): User?
}

@Service
class DefaultUserService(private val repository: CoroutineUserRepository) : UserService {
  override suspend fun findUserById(id: Long): UserService.User? =
    repository.findById(id)?.toDomain()

  private fun UserDTO.toDomain(): UserService.User = UserService.User(username, email)
}
