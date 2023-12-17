package naps.domain.health

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import naps.config.Dependencies
import naps.domain.naps.napRoutes

object Routes {
    @Resource("/health")
    class Health
}

fun Application.routerConfig(
    dependencies: Dependencies
): Routing = routing {
    healthRoutes()
    napRoutes(dependencies.napService)
}

fun Routing.healthRoutes() {
    get<Routes.Health> {
        val response = getHealthResponse(true, "Some error")
        val status = getHealthStatus(response)
        call.respond(status, response)
    }
}

fun getHealthResponse(healthy: Boolean, error: String) =
    if (healthy) HealthResponse.Healthy() else HealthResponse.Unhealthy(error)

fun getHealthStatus(response: HealthResponse) =
    when (response) {
        is HealthResponse.Healthy -> HttpStatusCode.OK
        is HealthResponse.Unhealthy -> HttpStatusCode.ServiceUnavailable
    }
