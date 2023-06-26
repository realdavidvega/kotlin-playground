import arrow.fx.coroutines.ExitCase
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.parTraverse
import models.Band
import models.BandMember
import models.BandMembers
import models.Instrument

// Resource safety
// Resource
// It is a data type allowing you to do declare how to acquire and release a resource, and then pass it around for
// usability or compose over it. It encodes the same concern as bracket and bracketCase.

//suspend fun main(): Unit {
//    val r = Resource({ res }, { _ -> res.dispose() })
//    r.use { /* use res */ }
//    r.use { /* use res again */ }
//    r.use { /* use res one more time */ }
//}

// This pattern leverages usability thanks to the fact that Resource is an instance.

// This program always dispose the resource on completion, and we've got the chance to do different things
// according to the different exit cases.

//////////////////////

val someError = RuntimeException("Boom!")

class MusicDatabase private constructor() {

    private val raul = BandMember(id = "2", name = "Raúl", instrument = Instrument.Guitar())
    private val benjy = BandMember(id = "3", name = "Benjy Montoya", instrument = Instrument.Microphone())
    private val simon = BandMember(id = "1", name = "Simon", instrument = Instrument.Drums())
    private val bandMembers = BandMembers(raul, benjy, simon)

    private val band1 = Band("Benjy Montoya & The Free Monads", "rock", bandMembers)
    private val band2 = Band("Some rap band", "rap", bandMembers)
    private val band3 = Band("Some reggaeton band", "reggaeton", bandMembers)

    companion object {
        suspend fun open(): MusicDatabase = MusicDatabase().also { println("DB opened") }
    }

    suspend fun close(): Unit = println("DB closed")

    suspend fun loadBand(name: String): Band = when (name) {
        "Benjy Montoya & The Free Monads" -> band1
        "Some rap band" -> band2
        "Some reggaeton band" -> band3
        else -> band3
    }
}

// Acquiring the resource. I.e: opens the database.
// Closing the database at the end.
// Also calling the corresponding callbacks on release depending on the exitCase:
// ExitCase.Completed: Calls completedCb passing the completed exit case.
// ExitCase.Canceled: Calls canceledCb passing the canceled exit case.
// ExitCase.Failure: Calls errorCb passing the error exit case.
// Traverses the list of names applying an effect. The effect has to use the resource, and load the band by the
// corresponding name for that traverse iteration. It'll return a list of all the loaded Bands in the end.
suspend fun someProgram1(
    names: List<String>,
    completedCb: (ExitCase.Completed) -> Unit,
    canceledCb: (ExitCase.Cancelled) -> Unit,
    errorCb: (ExitCase.Failure) -> Unit
): List<Band> {
    val res = Resource(
        acquire = { MusicDatabase.open() },
        release = { db, exitCase ->
            when (exitCase) {
                is ExitCase.Completed -> { completedCb(exitCase) }
                is ExitCase.Cancelled -> { canceledCb(exitCase) }
                is ExitCase.Failure -> { errorCb(exitCase) }
            }
            db.close()
        }
    )
    return names.parTraverse { name ->
        res.use { db -> db.loadBand(name) }
    }

    // more efficient alternative
    //return res.use {db -> names.parTraverse { name -> db.loadBand(name) }}
}

suspend fun main() {
    val names = listOf("Benjy Montoya & The Free Monads", "Some rap band", "Some reggaeton band")
    val bands = someProgram1(names, { println(it) }, { println(it) }, { println(it) })
}
