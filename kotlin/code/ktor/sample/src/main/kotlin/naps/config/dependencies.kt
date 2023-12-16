package naps.config

import naps.domain.naps.NapService
import naps.domain.naps.napMigrations
import naps.domain.naps.napPersistence
import naps.domain.naps.napService
import io.ktor.server.application.Application
import java.sql.Connection
import java.sql.DriverManager

class Dependencies(
    val napService: NapService
)

fun Application.dependencies() : Dependencies {
    val connection = getDBConnection()

    napMigrations(connection)
    val napPersistence = napPersistence(connection)
    val napService = napService(napPersistence)

    return Dependencies(
        napService
    )
}

fun getDBConnection(
    url: String = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    user: String = "root",
    password: String = ""
): Connection = DriverManager.getConnection(url, user, password)
