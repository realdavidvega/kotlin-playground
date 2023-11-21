import arrow.fx.coroutines.parSequence
import arrow.fx.coroutines.parTraverse
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

// Arrow Fx Coroutines
// parTraverse & parSequence
// At runtime, it's common that the amount of operations you need to run in parallel is dynamic.
// You have parTraverse and parSequence for that.

// Here we have the same distinction we had for traverse and sequence
// If you want to traverse the whole structure and apply an effect using your own function, use parTraverse:

suspend fun op(a: Int): Nothing = TODO()

// If what you have is a list of effects, you can apply them in parallel using parSequence:
suspend fun effect1(): Nothing = TODO()
suspend fun effect2(): Nothing = TODO()
suspend fun effect3(): Nothing = TODO()

suspend fun otherRunnable2() {
    listOf(1, 2, 3).parTraverse { value -> op(value) }

    // with custom context
    // listOf(1, 2, 3).parTraverse(IOPool) { value -> op(value) }

    listOf(::effect1, ::effect2, ::effect3).parSequence()

    // with custom context
    //listOf(::effect1, ::effect2, ::effect3).parSequence(IOPool)
}

//////////////////////

suspend fun loadSpeakerById(id: String): Speaker =
    when (id) {
        "id1" -> Speaker("id1", "Simon")
        "id2" -> Speaker("id2", "Raul")
        else -> Speaker("id3", "Jorge")
    }

// it uses parTraverse to load speakers parallel by id.
suspend fun loadSpeakersInParTraverse(ids: List<String>): List<Speaker> =
    ids.parTraverse { id ->
        loadSpeakerById(id)
    }

// it uses parTraverse to load speakers parallel by id using the provided CoroutineContext.
suspend fun loadSpeakersInParTraverse(ctx: CoroutineContext, ids: List<String>): List<Speaker> =
    ids.parTraverse(ctx) {id ->
        loadSpeakerById(id)
    }

// it uses parSequence to load speakers parallel by id.
suspend fun loadSpeakersInParSequence(ids: List<String>): List<Speaker> =
    ids.map { id: String -> suspend { loadSpeakerById(id) } }.parSequence()

// it uses parSequence to load speakers parallel by id using the provided CoroutineContext.
suspend fun loadSpeakersInParSequence(ctx: CoroutineContext, ids: List<String>): List<Speaker> =
    ids.map { id: String -> suspend { loadSpeakerById(id) } }.parSequence(ctx)

suspend fun main() {
    val ids = listOf("id1", "id2", "id3")

    val speakers1 = loadSpeakersInParTraverse(ids)
    println(speakers1)

    val speakers2 = loadSpeakersInParTraverse(Dispatchers.IO, ids)
    println(speakers2)

    val speakers3 = loadSpeakersInParSequence(ids)
    println(speakers3)

    val speakers4 = loadSpeakersInParSequence(Dispatchers.IO, ids)
    println(speakers4)
}
