import arrow.fx.coroutines.parZip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Arrow Fx Coroutines
// ParZip
// Parallelization is a must on any concurrency library. We provide parZip for that. Here's how it works:

suspend fun op1(): Int = TODO()
suspend fun op2(): Boolean = TODO()
suspend fun op3(): String = TODO()

class SomeNewModel(a: Int, b: Boolean, c: String)

suspend fun anotherRunnable3() {
    parZip({ op1() }, { op2() }, { op3() }) { a, b, c ->
        SomeNewModel(a, b, c)
    }
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
    withContext(Dispatchers.IO) {
        listOf(
            Room("id11", "Room 11"),
            Room("id12", "Room 12"),
            Room("id13", "Room 13")
        )
    }

suspend fun loadVenues(): List<Venue> =
    withContext(Dispatchers.IO) {
        listOf(
            Venue("id111", "Venue 111", "1300 Columbus Ave, SF, CA 94133"),
            Venue("id222", "Venue 222", "498 Jefferson St, SF, CA 94109")
        )
    }

// it loads speakers, rooms and venues and composes them into an Event using parZip.
suspend fun loadEvent(): Event =
    parZip(
        { loadSpeakers() },
        { loadRooms() },
        { loadVenues() }
    ) {speakers, rooms, venues ->
        Event(speakers, rooms, venues)
    }

suspend fun main() {
    val event = loadEvent()
    println(event)
}
