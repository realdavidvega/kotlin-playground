import arrow.fx.coroutines.raceN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// Arrow Fx Coroutines
// raceN
// Sometimes we have different effects that we want to run parallel,
// and we just care about the one that completes first, so we discard the other ones.

// We can do that with raceN, that allows you to run N parallel effects (suspend ops) and keep the one
// that completes first.

suspend fun op4(): Nothing = TODO()
suspend fun op5(): Nothing = TODO()
suspend fun op6(): Nothing = TODO()

suspend fun someRunnable() {
    raceN({op4()}, {op5()}, {op6()}).fold(
        ifA = { /* ... */ },
        ifB = { /* ... */ },
        ifC = { /* ... */ }
    )
}

// Note how you can use fold to match over the returned value, providing lambdas for each one of the possible results.

//////////////////////

suspend fun loadSpeakersDelay(): List<Speaker> =
    withContext(Dispatchers.IO) {
        delay(200)
        listOf(
            Speaker("id1", "Simon"),
            Speaker("id2", "Jorge"),
            Speaker("id3", "Raul")
        )
    }

suspend fun loadRoomsDelay(): List<Room> =
    withContext(Dispatchers.IO) {
        delay(100)
        listOf(
            Room("id11", "Room 11"),
            Room("id12", "Room 12"),
            Room("id13", "Room 13")
        )
    }

suspend fun loadVenuesDelay(): List<Venue> =
    withContext(Dispatchers.IO) {
        delay(300)
        listOf(
            Venue("id111", "Venue 111", "1300 Columbus Ave, SF, CA 94133"),
            Venue("id222", "Venue 222", "498 Jefferson St, SF, CA 94109")
        )
    }

// it races requests to load speakers, rooms and venues parallel.
// folds over the result to map it to "Speakers", "Rooms", or "Venues" depending on the case.
suspend fun raceOps(): String =
    raceN(
        { loadSpeakersDelay() },
        { loadRoomsDelay() },
        { loadVenuesDelay() }
    ).fold(
        ifA = { "Speakers" },
        ifB = { "Rooms" },
        ifC = { "Venues" }
    )

suspend fun main() {
    val winner = raceOps()

    // Rooms
    println(winner)
}
