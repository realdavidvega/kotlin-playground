import arrow.fx.coroutines.parZip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

// Functional Streams
// Error handling
// As we have learned, there are multiple ways to encode error handling in a program based on functional streams.
// It will always depend on the errors your program needs to support, how you model those, and how much we want to
// segregate our error handling strategy.

// That said, one of the options is leveraging Flow error control based on Throwable.
// We can use Flow#catch for that purpose.

fun speakersFlow(): Flow<List<Speaker>> = flow {
    val speakers = loadSpeakers()
    emit(speakers)
}

suspend fun someProgram2() {
    speakersFlow() // This could fail 💥
        .catch { emit(emptyList()) } // captures all from upstream 👆👆👆
        .collect { println(it) }
}

//////////////////////

data class Speaker(val id: String, val name: String)
data class Room(val id: String, val name: String)
data class Venue(val id: String, val name: String, val address: String)
data class Event(val speakers: List<Speaker>, val rooms: List<Room>, val venues: List<Venue>)

suspend fun loadSpeakers(): List<Speaker> =
    withContext(Dispatchers.IO) {
        listOf(
            Speaker("id1", "Simon"),
            Speaker("id2", "Jorge"),
            Speaker("id3", "Raul")
        )
    }

suspend fun loadRooms(): List<Room> =
    throw IOException("Boom!")

suspend fun loadVenues(): List<Venue> =
    withContext(Dispatchers.IO) {
        listOf(
            Venue("id111", "Venue 111", "1300 Columbus Ave, SF, CA 94133"),
            Venue("id222", "Venue 222", "498 Jefferson St, SF, CA 94109")
        )
    }

// Creates a flow that runs the three provided suspended effects (loadSpeakers, loadRooms, loadVenues) in
// parallel and composes an Event with the three lists.
// Emits the resulting Event.
// Catches any errors happening on any of the described effects and provides a default
// Event(emptyList(), emptyList(), emptyList()) in that case.
// Remember parZip suspended operator to run N effects in parallel and combine results in the end.
suspend fun loadEvent(): Flow<Event> =
    flow {
        val event = parZip(
            { loadSpeakers() },
            { loadRooms() },
            { loadVenues() }
        ) { speakers, rooms, venues ->
            Event(speakers, rooms, venues)
        }
        emit(event)
    }.catch { emit(Event(emptyList(), emptyList(), emptyList())) }

suspend fun main() {
    loadEvent().collect { println(it) }
}
