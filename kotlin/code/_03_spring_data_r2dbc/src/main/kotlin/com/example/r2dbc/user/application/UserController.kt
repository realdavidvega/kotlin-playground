package com.example.r2dbc.user.application

import arrow.core.getOrElse
import arrow.core.raise.either
import com.example.r2dbc.user.domain.UserService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class UserController(private val service: UserService) {
  @GetMapping("/users/{id}")
  suspend fun getUserById(@PathVariable id: Long): ResponseEntity<UserService.User> =
    either { service.findUserById(id) }
      .map { user -> ResponseEntity.ok(user) }
      .getOrElse { error ->
        when (error) {
          UserService.Error.Internal -> ResponseEntity.internalServerError().build()
          UserService.Error.UserNotFound -> ResponseEntity.notFound().build()
        }
      }
}
