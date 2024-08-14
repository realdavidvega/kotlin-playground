@file:Suppress("Unused", "MagicNumber")

package coroutines

import java.lang.Thread.sleep
import java.time.Duration
import java.time.LocalDateTime
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono

/** 1. Continuations and coroutines basics using `kotlinx.coroutines`. */
object Continuations {

  // continuation example
  // has some context
  // can be resumed with a Result, which encapsulates a successful outcome T or failure Throwable
  interface MyContinuation<in T> {
    val context: CoroutineContext

    fun resumeWith(result: Result<T>)

    fun resume(t: T): Unit = resumeWith(Result.success(t))

    fun resumeWithException(exception: Throwable): Unit = resumeWith(Result.failure(exception))
  }

  val myContinuation: MyContinuation<Unit> =
    object : MyContinuation<Unit> {
      override val context: CoroutineContext = EmptyCoroutineContext

      override fun resumeWith(result: Result<Unit>) = TODO()
    }

  // blocking vs. suspending
  private const val MILLIS = 1000L

  private fun blocking() {
    sleep(MILLIS)
    println("Blocking finished at ${LocalDateTime.now()}")
  }

  private fun reactive(): Mono<Unit> =
    Mono.delay(Duration.ofMillis(MILLIS)).flatMap {
      Mono.fromCallable { println("Reactor finished at ${LocalDateTime.now()}") }
    }

  private suspend fun suspending() {
    delay(MILLIS)
    println("Suspend finished at ${LocalDateTime.now()}")
  }

  @JvmStatic
  fun main(args: Array<String>) {
    println("Blocking started at ${LocalDateTime.now()}").also { blocking() }
    println("Reactor started at ${LocalDateTime.now()}").also { reactive().block() }
    runBlocking { println("Suspend started at ${LocalDateTime.now()}").also { suspending() } }
  }
}

// coroutines
typealias Coroutine = Continuation<Unit>

object Coroutines {

  // some playground.suspending block
  private val suspending: suspend () -> Int = suspend { 1 }

  // playground.getContinuation
  private val continuation: Continuation<Int> =
    Continuation(EmptyCoroutineContext) { println("The coroutines.Coroutine finished with $it") }

  // playground.getCoroutine wired to a playground.getContinuation
  val coroutine: Continuation<Unit> = suspending.createCoroutine(continuation)

  private fun <T> (suspend () -> T).createCoroutine(
    coroutineContext: CoroutineContext,
    lambda: (Result<T>) -> Unit,
  ): Coroutine = createCoroutine(Continuation(coroutineContext, lambda))

  private fun Coroutine.start(): Unit = resume(Unit)

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      println("Start rockthejvm.playground.main at ${LocalDateTime.now()}")
      // some other playground.suspending block
      val someBlock: suspend () -> Unit = {
        println("Some block started at ${LocalDateTime.now()}")
        delay(1000L)
        println("Some block finished at ${LocalDateTime.now()}")
      }

      val someCoroutine: Coroutine =
        someBlock.createCoroutine(EmptyCoroutineContext) {
          println("The playground.getCoroutine finished at ${LocalDateTime.now()}")
        }

      println("Start playground.getCoroutine at playground.main at ${LocalDateTime.now()}")
      someCoroutine.start()

      println("Delay at playground.main at ${LocalDateTime.now()}")
      delay(1000L)
      println("Finish playground.main at ${LocalDateTime.now()}")
    }
  }
}

// coroutine contexts
interface MyCoroutineContext {
  operator fun <E : Element> get(key: Key<E>): E?

  fun <R> fold(initial: R, operation: (R, Element) -> R): R

  operator fun plus(context: MyCoroutineContext): MyCoroutineContext

  fun minusKey(key: Key<*>): MyCoroutineContext

  interface Key<E : Element>

  interface Element : MyCoroutineContext {
    val key: Key<*>
  }
}

typealias TheCoroutineContext =
  Map<MyCoroutineContext.Key<MyCoroutineContext.Element>, MyCoroutineContext.Element>

// custom playground.getCoroutine context
data class SomeCoroutine(val name: String) : AbstractCoroutineContextElement(SomeCoroutine) {
  companion object Key : CoroutineContext.Key<SomeCoroutine>

  override fun toString(): String = "CoroutineName($name)"
}

suspend fun name(): String? = coroutineContext[SomeCoroutine]?.name
