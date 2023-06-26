package naps.config

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.resources.Resources
import naps.domain.health.routerConfig

fun Application.module() {
    val dependencies = dependencies()
    serverConfig()
    routerConfig(dependencies)
}

fun Application.serverConfig() {
    install(Resources)
    install(DefaultHeaders)
    install(ContentNegotiation) {
        json()
    }
}
