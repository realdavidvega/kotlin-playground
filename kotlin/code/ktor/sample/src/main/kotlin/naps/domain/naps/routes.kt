package naps.domain.naps

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.resources.post
import io.ktor.server.resources.delete
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalTime

object Routes {
    @Resource("/naps")
    class Naps {
        @Resource("{id}")
        data class ById(val parent: Naps = Naps(), val id: Long)
    }
}

@Serializable
data class NapTime(
    @Contextual
    val from: LocalTime,
    @Contextual
    val to: LocalTime,
)

fun Routing.napRoutes(napService: NapService) {
    post<Routes.Naps> {
        val napTime = call.receive<NapTime>()
        val id = napService.createNap(napTime)
        call.respond(HttpStatusCode.Created, id)
    }

    get<Routes.Naps.ById> { req ->
        val nap = napService.getNap(req.id)
        call.respond(HttpStatusCode.OK, nap)
    }

    delete<Routes.Naps.ById> { req ->
        napService.deleteNap(req.id)
        call.respond(HttpStatusCode.NoContent)
    }
}
