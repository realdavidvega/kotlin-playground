package playground

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono
import java.lang.Thread.sleep
import java.time.Duration
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// Continuation
// has some context
// can be resumed with a Result, which encapsulates a successful outcome T or failure Throwable
interface MyContinuation<in T> {
    val context: CoroutineContext

    fun resumeWith(result: Result<T>)

    fun resume(t: T) =
        resumeWith(Result.success(t))

    fun resumeWithException(exception: Throwable): Unit =
        resumeWith(Result.failure(exception))
}

val myContinuation: MyContinuation<Unit> = object : MyContinuation<Unit> {
    override val context: CoroutineContext = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) = TODO()
}


// Blocking vs. Suspending
const val MILLIS = 1000L

fun blocking() {
    sleep(MILLIS)
    println("Blocking finished at ${LocalDateTime.now()}")
}

fun reactive(): Mono<Unit> =
    Mono.delay(Duration.ofMillis(MILLIS)).flatMap {
        Mono.fromCallable {
            println("Reactor finished at ${LocalDateTime.now()}")
        }
    }

suspend fun suspending() {
    delay(MILLIS)
    println("Suspend finished at ${LocalDateTime.now()}")
}

fun main() {
    println("Blocking started at ${LocalDateTime.now()}").also { blocking() }
    println("Reactor started at ${LocalDateTime.now()}").also {  reactive().block() }
    runBlocking { println("Suspend started at ${LocalDateTime.now()}").also { suspending() } }
}
