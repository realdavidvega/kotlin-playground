package shopping

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Routing.shoppingRoutes(
    service: ShoppingService
) {
    route(ShoppingListItem.path) {
        get {
            val list = service.listAll()
            call.respond(list)
        }
        post {
            service.create(call.receive<ShoppingListItem>())
            call.respond(HttpStatusCode.OK)
        }
        delete("/{id}") {
            val id = call.parameters["id"] ?: error("Invalid delete request")
            service.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
