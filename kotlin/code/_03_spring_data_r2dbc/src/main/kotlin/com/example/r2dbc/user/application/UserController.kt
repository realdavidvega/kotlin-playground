package com.example.r2dbc.user.application

import com.example.r2dbc.user.domain.UserService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class UserController(private val service: UserService) {
  @GetMapping("/users/{id}")
  suspend fun getUserById(@PathVariable id: Long): ResponseEntity<UserService.User> {
    val user = service.findUserById(id)
    return if (user != null) ResponseEntity.ok(user) else ResponseEntity.notFound().build()
  }
}
