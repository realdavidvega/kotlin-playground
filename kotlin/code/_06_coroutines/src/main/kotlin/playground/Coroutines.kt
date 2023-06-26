package playground

import kotlinx.coroutines.delay
import java.time.LocalDateTime
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

// some playground.suspending block
val suspending: suspend () -> Int = suspend { 1 }

// playground.getContinuation
val continuation: Continuation<Int> = Continuation(EmptyCoroutineContext) {
    println("The playground.Coroutine finished with $it")
}

// playground.getCoroutine wired to a playground.getContinuation
val coroutine: Continuation<Unit> =
    suspending.createCoroutine(continuation)

typealias Coroutine = Continuation<Unit>

fun <T> (suspend () -> T).createCoroutine(
    coroutineContext: CoroutineContext,
    lambda: (Result<T>) -> Unit
): Coroutine = createCoroutine(Continuation(coroutineContext, lambda))

fun Coroutine.start(): Unit = resume(Unit)

suspend fun main() {
    println("Start rockthejvm.playground.main at ${LocalDateTime.now()}")
    // some other playground.suspending block
    val someBlock: suspend () -> Unit = {
        println("Some block started at ${LocalDateTime.now()}")
        delay(1000L)
        println("Some block finished at ${LocalDateTime.now()}")
    }

    val someCoroutine: Coroutine =
        someBlock.createCoroutine(
            EmptyCoroutineContext
        ) {
            println("The playground.getCoroutine finished at ${LocalDateTime.now()}")
        }

    println("Start playground.getCoroutine at rockthejvm.playground.main at ${LocalDateTime.now()}")
    someCoroutine.start()

    println("Delay at rockthejvm.playground.main at ${LocalDateTime.now()}")
    delay(1000L)
    println("Finish rockthejvm.playground.main at ${LocalDateTime.now()}")
}
