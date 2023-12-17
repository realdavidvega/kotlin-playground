package naps.integration.kotest

import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import naps.config.dependencies
import naps.config.module
import naps.config.napStub
import naps.config.serverConfig
import naps.domain.health.routerConfig
import naps.domain.naps.NapTime

class NapServiceIntegrationKotest : StringSpec({
    val dependencies = dependencies()

    "Should create nap" {
        testApplication {
            application {
                serverConfig()
                routerConfig(dependencies)
            }

            // given
            val service = dependencies.napService

            // when
            val id = service.createNap(napStub)

            // then
            id shouldBe 1L
        }
    }

    "Should get nap" {
        testApplication {
            application {
                serverConfig()
                routerConfig(dependencies)
            }

            // given
            val service = dependencies.napService
            val id = service.createNap(napStub)

            // when
            val napTime: NapTime = service.getNap(id)

            // then
            napTime shouldBe napStub
        }
    }
})
