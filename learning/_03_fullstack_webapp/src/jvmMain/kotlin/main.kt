import env.Dependencies
import env.Env
import env.dependencies
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import shopping.shoppingRoutes
import statics.staticsRoutes

fun main() {
    val env = Env()
    val dependencies = dependencies(env)

    embeddedServer(
        factory = Netty,
        port = env.http.port,
        host = env.http.host,
        module = { module(dependencies) }
    ).start(wait = true)
}

fun Application.module(dependencies: Dependencies) {
    plugins()
    router(dependencies)
}

fun Application.plugins() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        anyHost()
    }
    install(Compression) {
        gzip()
    }
}

fun Application.router(dependencies: Dependencies) {
    routing {
        staticsRoutes()
        shoppingRoutes(dependencies.shoppingService)
    }
}
