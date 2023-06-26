
package naps.unit.kotest

import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.StringSpec
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import naps.config.Dependencies
import naps.config.napServiceStub
import naps.config.napStub
import naps.config.serverConfig
import naps.domain.health.routerConfig

class RoutesUnitKotlinTest  : StringSpec({
    val service = napServiceStub()

    "Should create account" {
        testApplication {
            application {
                val dependencies = Dependencies(service)
                serverConfig()
                routerConfig(dependencies)
            }

            client.post("/naps") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(napStub))
            }.apply {
                shouldHaveStatus(HttpStatusCode.Created)
            }
        }
    }
})