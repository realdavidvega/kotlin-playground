import arrow.core.Either
import arrow.fx.coroutines.ExitCase
import arrow.fx.coroutines.bracketCase
import models.Band
import models.BandMember
import models.BandMembers
import models.Instrument

// Resource safety
// Bracket Case

// It makes us able to take different release actions depending on how the use step completed.
// It uses ExitCase to differentiate.

suspend fun someProgram2(): Unit {
    val some = SomeResource
    bracketCase(
        acquire = { some },
        use = { res -> /* do something with r */ },
        release = { res, exitCase ->
            when (exitCase) {
                is ExitCase.Completed -> { /* do something */ }
                is ExitCase.Cancelled -> { /* do something */ }
                is ExitCase.Failure -> { /* do something */ }
            }
            res.dispose()
        }
    )
}

// This program always releases the resource on completion, and we've got the chance to do different things
// according to the different exit cases.

//////////////////////

val error = RuntimeException("Boom!")

class OtherDatabase private constructor() {

    private val raul = BandMember(id = "2", name = "Raúl", instrument = Instrument.Guitar())
    private val benjy = BandMember(id = "3", name = "Benjy Montoya", instrument = Instrument.Microphone())
    private val simon = BandMember(id = "1", name = "Simon", instrument = Instrument.Drums())
    private val bandMembers = BandMembers(raul, benjy, simon)

    private val band = Band("Benjy Montoya & The Free Monads", "rock", bandMembers)

    companion object {
        suspend fun open(): OtherDatabase = OtherDatabase().also { println("DB opened") }
    }

    suspend fun close(): Unit = println("DB closed")

    suspend fun loadBand(name: String): Band = throw error
}

suspend fun someProgram(
    name: String,
    completedCb: (ExitCase.Completed) -> Unit,
    canceledCb: (ExitCase.Cancelled) -> Unit,
    errorCb: (ExitCase.Failure) -> Unit
): Band =
    bracketCase(
        acquire = { OtherDatabase.open() },
        use = { db -> db.loadBand(name) },
        release = { db, exitCase ->
            when(exitCase) {
                is ExitCase.Completed -> { completedCb(exitCase) }
                is ExitCase.Cancelled -> { canceledCb(exitCase) }
                is ExitCase.Failure -> { errorCb(exitCase) }
            }
            db.close()
        }
    )

suspend fun main() {
    val band = Either.catch {
        someProgram(
            "Some band",
            { println("Completed! $it")},
            { println("Cancelled! $it")},
            { println("Failure! $it")}
        )
    }
}
