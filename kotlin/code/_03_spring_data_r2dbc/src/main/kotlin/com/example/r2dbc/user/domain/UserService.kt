package com.example.r2dbc.user.domain

import arrow.core.raise.Raise
import com.example.r2dbc.user.infrastructure.CoroutineUserRepository
import com.example.r2dbc.user.infrastructure.UserDTO
import org.springframework.stereotype.Service

interface UserService {
  data class User(val username: String, val email: String)

  sealed class Error(val message: String) {
    data object UserNotFound : Error("User not found.")
    data object Internal : Error("Internal error.")
  }

  context(Raise<Error>)
  suspend fun findUserById(id: Long): User
}

@Service
class DefaultUserService(private val repository: CoroutineUserRepository) : UserService {
  context(Raise<UserService.Error>)
  override suspend fun findUserById(id: Long): UserService.User =
    repository.findById(id)?.toDomain() ?: raise(UserService.Error.UserNotFound)

  private fun UserDTO.toDomain(): UserService.User = UserService.User(username, email)
}
