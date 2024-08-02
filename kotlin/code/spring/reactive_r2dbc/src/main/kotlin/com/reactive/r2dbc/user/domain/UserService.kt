package com.reactive.r2dbc.user.domain

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.ensureNotNull
import com.reactive.r2dbc.user.infrastructure.DefaultUserRepository
import com.reactive.r2dbc.user.infrastructure.UserDTO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.springframework.stereotype.Service

interface UserService {
  data class User(val username: String, val email: String)

  sealed class Error(val message: String) {
    data object UserNotFound : Error("User not found")

    data object Internal : Error("Internal error")
  }

  context(Raise<Error.Internal>)
  suspend fun createUser(username: String, email: String): Long

  context(Raise<Error>)
  suspend fun findUserById(id: Long): User

  context(Raise<Error.Internal>)
  fun findAllUsers(): Flow<User>
}

@Service
class DefaultUserService(private val repository: DefaultUserRepository) : UserService {

  context(Raise<UserService.Error.Internal>)
  override suspend fun createUser(username: String, email: String): Long =
    catch({
      val userDTO = UserDTO(username = username, email = email)
      ensureNotNull(repository.save(userDTO)) { raise(UserService.Error.Internal) }
    }) {
      raise(UserService.Error.Internal)
    }

  context(Raise<UserService.Error>)
  override suspend fun findUserById(id: Long): UserService.User =
    catch({ repository.findUserById(id)?.toDomain() ?: raise(UserService.Error.UserNotFound) }) {
      raise(UserService.Error.Internal)
    }

  context(Raise<UserService.Error.Internal>)
  override fun findAllUsers(): Flow<UserService.User> =
    catch({
      val users = repository.findAllUsers()
      users.map { user -> user.toDomain() }.onEach { delay(500) }
    }) {
      raise(UserService.Error.Internal)
    }

  private fun UserDTO.toDomain(): UserService.User = UserService.User(username, email)
}
