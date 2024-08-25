package coroutines

import arrow.fx.coroutines.CountDownLatch
import arrow.fx.coroutines.CyclicBarrier
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * (6) Concurrency Primitives
 *
 * Concurrency primitives like [CountDownLatch] and [CyclicBarrier] on coroutines using
 * `kotlinx.coroutines` and `arrow.fx.coroutines`.
 */
object Primitives {

  private const val TASKS = 5L

  /**
   * [CountDownLatch] allows for awaiting a given number of countdown signals.
   *
   * Works as a synchronization aid allowing one or more coroutines to wait without blocking until a
   * set of operations being performed in other coroutines complete.
   *
   * This type models the behavior of `java.util.concurrent.CountDownLatch`.
   */
  private val latch = CountDownLatch(TASKS)

  /** Class representing a worker which will do some work and then signal the latch */
  class Worker(private val workLog: MutableList<String>, private val latch: CountDownLatch) {
    suspend fun work() {
      delay(2.seconds.inWholeMilliseconds)
      workLog.add("Work done!")
      latch.countDown()
      println("Remaining tasks: ${latch.count()}")
    }
  }

  private const val CAPACITY = 5

  /**
   * [CyclicBarrier] is a synchronization mechanism that allows a set of coroutines to wait for each
   * other to reach a certain point before continuing execution. It is called cyclic because it can
   * be reused after all coroutines have reached the barrier and been released.
   *
   * To use a [CyclicBarrier], each coroutine must call the await method on the barrier object,
   * which will cause the coroutine to suspend until the required number of coroutines have reached
   * the barrier. Once all coroutines have reached the barrier, they resume execution.
   *
   * It has a `capacity` parameter that specifies the maximum number of coroutines that can be
   * waiting at any given time.
   *
   * It also has an optional `barrierAction` parameter that can be used to perform an action when
   * the barrier is cycled, but before releasing.
   *
   * This type models the behavior of `java.util.concurrent.CyclicBarrier`.
   */
  val barrier = CyclicBarrier(CAPACITY) { println("Cyclic barrier cycled, releasing!") }

  class Ant {
    suspend fun work() {
      delay(2.seconds.inWholeMilliseconds)
      println("I'm working... don't disturb me!")

      // When await is called the function will suspend until the required number of coroutines
      // have called await. Once the capacity of the barrier has been reached,
      // the coroutine will be released and continue execution
      barrier.await()
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val workLog = mutableListOf<String>()

      // we launch 5 coroutines that will do some concurrently work
      (1..5).forEach { _ -> launch { Worker(workLog, latch).work() } }

      // we wait for the work to be done using the latch
      println("Awaiting for work to be done...")
      latch.await()

      // we print the result
      println("Work log: $workLog")

      println("----------------------")

      // we launch 10 coroutines that will do some work concurrently
      // but only 5 can work at the same time, rest will await the barrier
      (1..10).forEach { _ -> launch { Ant().work() } }
    }
  }
}
