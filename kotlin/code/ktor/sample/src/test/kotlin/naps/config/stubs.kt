package naps.config

import io.kotest.core.TestConfiguration
import kotlinx.datetime.LocalTime
import naps.domain.naps.*
import java.sql.Connection
import java.sql.DriverManager

val napStub = NapTime(
    LocalTime(14, 40, 0, 0 ),
    LocalTime(15, 0, 0, 0 )
)

fun napServiceStub(
    createNap: suspend (napTime: NapTime) -> Long = { 1L },
    getNap: suspend (id: Long) -> NapTime = { napStub },
    deleteNap: suspend (id: Long) -> Boolean = { true },
): NapService = object : NapService {
    override suspend fun createNap(napTime: NapTime): Long = createNap(napTime)
    override suspend fun getNap(id: Long): NapTime = getNap(id)
    override suspend fun deleteNap(id: Long): Boolean = deleteNap(id)
}

fun napPersistenceStub(
    create: suspend(napTime: NapTime) -> Long = { 1L },
    read: suspend (id: Long) -> NapTime = { napStub },
    delete: suspend (id: Long) -> Boolean = { true }
): NapPersistence = object : NapPersistence {
    override suspend fun create(napTime: NapTime): Long = create(napTime)
    override suspend fun read(id: Long): NapTime = read(id)
    override suspend fun delete(id: Long): Boolean = delete(id)
}

fun TestConfiguration.database(): Connection {
    val connection = getDBConnection()
    afterTest { connection.close() }
    return connection
}

fun TestConfiguration.dependencies(): Dependencies {
    val connection = database()

    napMigrations(connection)
    val napPersistence = napPersistence(connection)
    val napService = napService(napPersistence)

    return Dependencies(
        napService
    )
}