package naps.integration.kotlintest

import naps.config.module
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
import naps.config.napStub
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutesIntegrationKotlinTest {

    @Test
    fun `Should create nap`() = testApplication {
        application {
            module()
        }

        client.post("/naps") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(napStub))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            assertEquals("1", bodyAsText())
        }
    }

    @Test
    fun `Should get nap`() = testApplication {
        application {
            module()
        }

        client.post("/naps") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(napStub))
        }

        client.get("/naps/1").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(napStub, Json.decodeFromString(body()))
        }
    }

    @Test
    fun `Should delete account`() = testApplication {
        application {
            module()
        }

        client.post("/naps") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(napStub))
        }

        client.delete("/naps/1").apply {
            assertEquals(HttpStatusCode.NoContent, status)
        }
    }
}
