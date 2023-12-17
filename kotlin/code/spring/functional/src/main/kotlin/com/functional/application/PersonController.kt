package com.functional.application

import arrow.core.getOrElse
import arrow.core.raise.either
import com.functional.domain.PersonHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

typealias Router = RouterFunction<ServerResponse>

interface PersonController {
    companion object {
        operator fun invoke(handler: PersonHandler): Router =
            coRouter {
                "/persons".nest {
                    GET("") {
                        either {
                            val persons = handler.readAll()
                            ok().contentType(APPLICATION_JSON).bodyAndAwait(persons)
                        }.getOrElse {
                            ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()
                        }
                    }
                    GET("/{id}") { request ->
                        either {
                            val id = request.pathVariable("id").toLong()
                            val person = handler.readOne(id)
                            ok().contentType(APPLICATION_JSON).bodyValueAndAwait(person)
                        }.getOrElse { error ->
                            when (error) {
                                is PersonHandler.Error.GenericError ->
                                    ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).buildAndAwait()

                                is PersonHandler.Error.PersonNotFound ->
                                    ServerResponse.notFound().buildAndAwait()
                            }
                        }
                    }
                }
            }
    }
}
