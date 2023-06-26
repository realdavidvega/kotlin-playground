package naps

import com.typesafe.config.ConfigFactory
import naps.config.module
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val config = ConfigFactory.load()
    val host = config.getString("ktor.deployment.host")
    val port = config.getInt("ktor.deployment.port")

    embeddedServer(
        factory = Netty,
        host = host,
        port = port,
        module = Application::module
    ).start(wait = true)
}
