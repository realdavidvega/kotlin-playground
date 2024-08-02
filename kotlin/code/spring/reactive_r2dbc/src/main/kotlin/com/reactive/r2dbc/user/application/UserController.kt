package com.reactive.r2dbc.user.application

import arrow.core.raise.recover
import com.reactive.r2dbc.user.domain.UserService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class UserController(private val service: UserService) {
  @PostMapping("/users")
  suspend fun createUser(@RequestBody body: UserService.User): ResponseEntity<Long> =
    recover({
      val user = service.createUser(username = body.username, email = body.email)
      ResponseEntity.ok(user)
    }) {
      ResponseEntity.internalServerError().build()
    }

  @GetMapping("/users/{id}")
  suspend fun getUserById(@PathVariable id: Long): ResponseEntity<UserService.User> =
    recover({
      val user = service.findUserById(id)
      ResponseEntity.ok(user)
    }) { error ->
      when (error) {
        UserService.Error.Internal -> ResponseEntity.internalServerError().build()
        UserService.Error.UserNotFound -> ResponseEntity.notFound().build()
      }
    }

  @GetMapping("/users", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
  suspend fun getAllUsers(): ResponseEntity<Flow<UserService.User>> =
    recover({
      val users = service.findAllUsers()
      ResponseEntity.ok(users)
    }) {
      ResponseEntity.internalServerError().build()
    }
}
