import arrow.core.Either
import arrow.fx.coroutines.parTraverse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import models.*

// Functional Streams
// Evaluating effects
// We understand "functional streams" as streams able to evaluate side effects and emit their results.
// We can write those effects using suspend and leverage Arrow Fx operators as needed.

// Here is an example of a Flow making use of Either.catch, which is a suspend function meant for
// catching errors in effects.
fun loadTalks(ids: List<TalkId>): Flow<Either<TalksNotFound, List<Talk>>> =
    flow {
        val maybeTalks = Either.catch { fetchTalksFromNetwork(ids) }
            .mapLeft { TalksNotFound }

        emit(maybeTalks)
    }.flowOn(Dispatchers.IO)

suspend fun someProgram() {
    loadTalks(listOf("talk1", "talk2"))
        .collect { println(it) } // Right([Talk(...), Talk(...)])
}

//////////////////////

private val raul = BandMember(id = "2", name = "Raúl", instrument = Instrument.Guitar())
private val benjy = BandMember(id = "3", name = "Benjy Montoya", instrument = Instrument.Microphone())
private val simon = BandMember(id = "1", name = "Simon", instrument = Instrument.Drums())
private val bandMembers = BandMembers(raul, benjy, simon)

private val band1 = Band("Benjy Montoya & The Free Monads", "rock", bandMembers)
private val band2 = Band("Some rap band", "rap", bandMembers)
private val band3 = Band("Some reggae band", "reggae", bandMembers)

suspend fun loadBand(name: String): Band = when (name) {
    "Benjy Montoya & The Free Monads" -> band1
    "Some rap band" -> band2
    "Some reggae band" -> band3
    else -> band3
}

fun loadDiscographies(bands: List<Band>): Flow<Discography> = flow {
    bands.forEach { band ->
        emit(
            when (band.name) {
                "Benjy Montoya & The Free Monads" -> Discography(
                    albums = listOf(
                        Album("Disc 1", listOf("Rock 1", "Rock 2", "Rock 3")),
                        Album("Disc 2", listOf("Awesome rock", "Incredible rock"))
                    )
                )
                "Some rap band" -> Discography(
                    albums = listOf(
                        Album("Rap disc 1", listOf("Rap 1", "Rap 2", "Rap 3"))
                    )
                )
                "Some reggae band" -> Discography(
                    albums = listOf(
                        Album("Reggae disc 1", listOf("Reggae 1", "Reggae 2", "Reggae 3")),
                        Album("Reggae disc 2", listOf("Awesome reggae", "Incredible reggae"))
                    )
                )
                else -> Discography(emptyList())
            }
        )
    }
}

// Creates a flow that traverses the list of names running a suspended effect for each, all of them in parallel.
// The effect will be the loadBand call to load the Band by its name.
// Emit the result of the effect over the stream.
// Chain a new effect after the bands are loaded. This one comes in the form of a Flow already: loadDiscographies.
// That will load and emit the discography of each Band a separate element, yielding a Flow<Discography>.
// Finally, we want to map each emitted Discography to its list of albums.
// The overall result will be a Flow<List<Album>> so it will emit the list of albums for each Discography.
// Remember parTraverse suspended operator to iterate over a collection and run one effect per element,
// all of them in parallel?
@FlowPreview
suspend fun albumsFlow(bandNames: List<String>): Flow<List<Album>> =
    flow {
        val bands = bandNames.parTraverse { loadBand(it) }
        emit(bands)
    }.flatMapMerge { loadDiscographies(it) }
        .map { it.albums }

@OptIn(FlowPreview::class)
suspend fun main() {
    val names = listOf("Benjy Montoya & The Free Monads", "Some rap band", "Some reggae band")
    albumsFlow(names).collect { println(it) }
}
