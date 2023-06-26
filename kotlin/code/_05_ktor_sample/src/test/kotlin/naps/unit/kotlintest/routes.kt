
package naps.unit.kotlintest

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import naps.config.Dependencies
import naps.config.napServiceStub
import naps.config.napStub
import naps.config.serverConfig
import naps.domain.health.routerConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutesUnitKotlinTest {

    @Test
    fun `Should create nap when valid local times`() {
        val service = napServiceStub()

        testApplication {
            application {
                val dependencies = Dependencies(service)
                serverConfig()
                routerConfig(dependencies)
            }

            // when
            client.post("/naps") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(napStub))
            }.apply {
                // then
                assertEquals(HttpStatusCode.Created, status)
                assertEquals("1", bodyAsText())
            }
        }
    }

    @Test
    fun `Should retrieve nap when valid nap id`() {
        val service = napServiceStub()

        testApplication {
            application {
                val dependencies = Dependencies(service)
                serverConfig()
                routerConfig(dependencies)
            }

            // when
            client.get("/naps/1").apply {
                // then
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(napStub, Json.decodeFromString(body()))
            }
        }
    }

    @Test
    fun `Should delete account`() = testApplication {
        val service = napServiceStub()

        application {
            val dependencies = Dependencies(service)
            serverConfig()
            routerConfig(dependencies)
        }

        // when
        client.delete("/naps/1").apply {
            // then
            assertEquals(HttpStatusCode.NoContent, status)
        }
    }
}
