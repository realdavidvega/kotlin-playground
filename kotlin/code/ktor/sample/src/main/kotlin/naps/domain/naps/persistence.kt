package naps.domain.naps

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime
import java.sql.Connection
import java.sql.Statement

interface NapPersistence {
    suspend fun create(napTime: NapTime): Long
    suspend fun read(id: Long): NapTime
    suspend fun delete(id: Long): Boolean
}

object Queries {
    const val CREATE_TABLE_NAPS =
        "CREATE TABLE IF NOT EXISTS naps (id INT PRIMARY KEY AUTO_INCREMENT, fromTime VARCHAR(255), toTime VARCHAR(255))"
    const val SELECT_NAP_BY_ID = "SELECT fromTime, toTime FROM naps WHERE id = ?"
    const val INSERT_NAP = "INSERT INTO naps (fromTime, toTime) VALUES (?, ?)"
    const val DELETE_NAP = "DELETE FROM naps WHERE id = ?"
}

fun napMigrations(connection: Connection) {
    val statement = connection.createStatement()
    statement.executeUpdate(Queries.CREATE_TABLE_NAPS)
}

@Suppress("TooGenericExceptionThrown")
fun napPersistence(
    connection: Connection,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): NapPersistence = object : NapPersistence {
    override suspend fun create(napTime: NapTime): Long = withContext(dispatcher) {
        val statement = connection.prepareStatement(Queries.INSERT_NAP, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, napTime.from.toString())
        statement.setString(2, napTime.to.toString())
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getLong(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted nap")
        }
    }

    override suspend fun read(id: Long): NapTime = withContext(dispatcher) {
        val statement = connection.prepareStatement(Queries.SELECT_NAP_BY_ID)
        statement.setLong(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val fromTime = LocalTime.parse(resultSet.getString("fromTime"))
            val toTime = LocalTime.parse(resultSet.getString("toTime"))

            return@withContext NapTime(fromTime, toTime)
        } else {
            throw Exception("Record not found")
        }
    }

    override suspend fun delete(id: Long): Boolean = withContext(dispatcher) {
        val statement = connection.prepareStatement(Queries.DELETE_NAP)
        statement.setLong(1, id)
        statement.executeUpdate()
        return@withContext true
    }
}
