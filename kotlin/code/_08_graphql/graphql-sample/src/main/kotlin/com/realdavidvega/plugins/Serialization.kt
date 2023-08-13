package com.realdavidvega.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing


fun Application.configureSerialization() {
  routing { get("/json/kotlinx-serialization") { call.respond(mapOf("hello" to "world")) } }
}
