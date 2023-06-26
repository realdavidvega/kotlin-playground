import arrow.core.*
import arrow.core.None.zip
import arrow.fx.coroutines.parZip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import models.Error
import java.io.IOException

// Functional Streams
// Error handling
// Working with errors in the context of a Flow varies a bit when we model effects using functional data types.

// If we use functional data types to catch errors happening in our effects, e.g: Validated.catch, Either.catch,
// ValidatedNel.catch, then our effects never throw anymore, and therefore we end up bypassing the Flow error control
// capabilities like the Flow#catch operator. Everything becomes happy path!

// This nesting of a stream datatype and a functional one has been traditionally used in frameworks like RxJava,
// where null values are not allowed so people ended up nesting Observable<Option<A>>.

// When we work like this we need to rely on happy path operators like map, or terminal operators like collect to
// handle or react to both sides of the operation.

fun speakersFlow2(): Flow<Either<Error.SpeakerNotFound, List<Speaker>>> =
    flow {
        val speakers = Either.catch { loadSpeakers() }
            .mapLeft { Error.SpeakerNotFound }

        emit(speakers)
    }

suspend fun someProgram3() {
    speakersFlow2()
        .map { it.handleError { emptyList() } }
        .collect {
            it.fold(
                ifLeft = { /* Will not reach this since we've recovered above */  },
                ifRight = { speakers -> println(speakers) }
            )
        }
}

// This is not wrong per se, just a different approach that's a trade-off to strongly type our errors by leveraging
// functional data types, and enhance explicitness plus exhaustive evaluation.

//////////////////////

suspend fun loadSpeakersNel(): ValidatedNel<Error.SpeakerNotFound, List<Speaker>> =
    ValidatedNel.catch {
        withContext(Dispatchers.IO) {
            listOf(
                Speaker("id1", "Simon"),
                Speaker("id2", "Jorge"),
                Speaker("id3", "Raul")
            )
        }
    }.mapLeft { Error.SpeakerNotFound.nel() }

suspend fun loadRoomsNel(): ValidatedNel<Error.RoomsNotFound, List<Room>> =
    ValidatedNel.catch { throw IOException("Boom!") }
        .mapLeft { Error.RoomsNotFound.nel() }

suspend fun loadVenuesNel(): ValidatedNel<Error.VenuesNotFound, List<Venue>> =
    ValidatedNel.catch {
        withContext(Dispatchers.IO) {
            listOf(
                Venue("id111", "Venue 111", "1300 Columbus Ave, SF, CA 94133"),
                Venue("id222", "Venue 222", "498 Jefferson St, SF, CA 94109")
            )
        }
    }.mapLeft { Error.VenuesNotFound.nel() }

// Builds a flow out of a suspended effect.
// The effect must be a zip of the three provided suspended effects (loadSpeakers, loadRooms, loadVenues)
// that combines all results into an Event. Don't forget to emit it!.
// Then it must map the flow to handle any errors on it by returning a default event like:
// Event(emptyList(), emptyList(), emptyList()).
// Finally pass the callback

suspend fun loadEvent(onResult: (Validated<NonEmptyList<Error>, Event>) -> Unit): Unit =
    flow {
        val event = loadSpeakersNel().zip(loadRoomsNel() , loadVenuesNel()) {speakers, rooms, venues ->
            Event(speakers, rooms, venues)
        }
        emit(event)
    }.map {
        it.handleError { Event(emptyList(), emptyList(), emptyList()) }
    }.collect { onResult(it) }

suspend fun main() {
    loadEvent { println(it) }
}
