package com.reactive.r2dbc.user.application

import arrow.core.raise.recover
import com.reactive.r2dbc.user.domain.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.json
import org.springframework.web.reactive.function.server.sse

/** First example of routes using Spring Controller with suspend functions and Flow */
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
  fun getAllUsers(): ResponseEntity<Flow<UserService.User>> =
    recover({
      val users = service.findAllUsers()
      ResponseEntity.ok(users)
    }) {
      ResponseEntity.internalServerError().build()
    }
}

/** Second example of routes using Spring Coroutines Router + Controller Handler */
@Configuration
class UserRouter {

  @Bean
  fun mainRouter(userHandler: UserHandler) = coRouter {
    accept(APPLICATION_JSON).nest {
      POST("/users", userHandler::createUser)
      GET("/users/{id}", userHandler::getUserById)
    }
    accept(TEXT_EVENT_STREAM).nest { GET("/users", userHandler::getAllUsers) }
  }
}

@Controller
class UserHandler(private val service: UserService) {

  suspend fun createUser(request: ServerRequest): ServerResponse =
    recover({
      val body = request.bodyToMono(UserService.User::class.java).awaitSingle()
      val user = service.createUser(username = body.username, email = body.email)
      ok().json().bodyValueAndAwait(user)
    }) {
      status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
    }

  suspend fun getUserById(request: ServerRequest): ServerResponse =
    recover({
      val id = request.pathVariable("id").toLong()
      val user = service.findUserById(id)
      ok().json().bodyValueAndAwait(user)
    }) { error ->
      when (error) {
        UserService.Error.Internal -> status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
        UserService.Error.UserNotFound -> status(HttpStatus.NOT_FOUND).buildAndAwait()
      }
    }

  suspend fun getAllUsers(request: ServerRequest): ServerResponse =
    recover({
      val users = service.findAllUsers()
      ok().sse().bodyAndAwait(users)
    }) {
      status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
    }
}
