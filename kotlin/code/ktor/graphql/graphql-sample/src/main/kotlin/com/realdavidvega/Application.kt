package com.realdavidvega

import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import com.realdavidvega.plugins.configureRouting
import com.realdavidvega.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.awaitCancellation

fun main(): Unit = SuspendApp {
  resourceScope {
    embeddedServer(factory = Netty, port = 8080, host = "0.0.0.0", module = Application::module)
      .start(wait = true)
  }
  awaitCancellation()
}

fun Application.module() {
  configureSerialization()
  configureRouting()
}
