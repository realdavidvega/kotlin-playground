package com.reactive.r2dbc.user.domain

import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.reactive.r2dbc.user.infrastructure.CoroutineUserRepository
import com.reactive.r2dbc.user.infrastructure.UserDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

interface UserService {
  data class User(val username: String, val email: String)

  sealed class Error(val message: String) {
    data object UserNotFound : Error("User not found.")

    data object Internal : Error("Internal error.")
  }

  context(Raise<Error>)
  suspend fun findUserById(id: Long): User

  context(Raise<Error.Internal>)
  fun findAllUsers(): Flow<User>
}

@Service
class DefaultUserService(private val repository: CoroutineUserRepository) : UserService {
  context(Raise<UserService.Error>)
  override suspend fun findUserById(id: Long): UserService.User =
      catch({ repository.findUserById(id)?.toDomain() ?: raise(UserService.Error.UserNotFound) }) {
        raise(UserService.Error.Internal)
      }

  context(Raise<UserService.Error.Internal>)
  override fun findAllUsers(): Flow<UserService.User> =
      catch({ repository.findAllUsers().map { user -> user.toDomain() } }) { raise(UserService.Error.Internal) }

  private fun UserDTO.toDomain(): UserService.User = UserService.User(username, email)
}
