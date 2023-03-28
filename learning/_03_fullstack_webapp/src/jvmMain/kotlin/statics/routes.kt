package statics

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.staticsRoutes() {
    get("/") {
        val index = this::class.java.classLoader.getResource("index.html") ?: error("Index not found.")
        call.respondText(
            index.readText(),
            ContentType.Text.Html
        )
    }
    static("/") {
        resources("")
    }
}
