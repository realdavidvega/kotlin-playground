package naps.integration.kotest

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
import naps.config.module
import naps.config.napStub

class NapRoutesIntegrationKotest : StringSpec({

    "Should create nap" {
        testApplication {
            application {
                module()
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
