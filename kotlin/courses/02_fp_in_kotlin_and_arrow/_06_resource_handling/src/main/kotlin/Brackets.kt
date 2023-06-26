import arrow.fx.coroutines.bracket
import models.Band
import models.BandMember
import models.BandMembers
import models.Instrument

// Resource safety
// bracket

// 1. Acquire a resource
// 2. Use or consume the resource
// 3. Release the resource
// This process needs to happen when you want to work safely with a resource.

// bracket makes us able to release resources no matter what happens while using them.
object SomeResource {
    suspend fun dispose(): Unit = TODO()
}

suspend fun someMain(): Unit {
    val res = SomeResource
    bracket(
        acquire = { res },
        use = { res -> /* do something with r */ },
        release = { res -> res.dispose() }
    )
}

// There are always three possible ending scenarios on usage:
// 1. use completes successfully.
// 2. use throws an error.
// 3. use gets cancelled.

// For all the three cases we want to close the resource at the end to not compromise program's integrity.

//////////////////////

class Database private constructor() {

    private val raul = BandMember(id = "2", name = "Raúl", instrument = Instrument.Guitar())
    private val benjy = BandMember(id = "3", name = "Benjy Montoya", instrument = Instrument.Microphone())
    private val simon = BandMember(id = "1", name = "Simon", instrument = Instrument.Drums())
    private val bandMembers = BandMembers(raul, benjy, simon)

    private val band = Band("Benjy Montoya & The Free Monads", "rock", bandMembers)

    companion object {
        suspend fun open(): Database = Database().also { println("DB opened") }
    }

    suspend fun close(): Unit = println("DB closed")

    suspend fun loadBand(name: String): Band = band.copy(name = name)
}

// Calls the function to open the database.
// Loads band details from the database using its name.
// Ensures to close the database at the end.
// Calls the release token at the end, so our test can validate the implementation.
suspend fun program(name: String, releaseToken: () -> Unit): Band =
    bracket(
        acquire = { Database.open() },
        use = { db -> db.loadBand(name) },
        release = { db ->
            db.close()
            releaseToken()
        }
    )

suspend fun main() {
    val band = program("Some band") { println("released") }
    println(band)
}
