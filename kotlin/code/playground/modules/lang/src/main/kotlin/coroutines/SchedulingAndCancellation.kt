@file:Suppress("Unused", "MagicNumber")

package coroutines

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Scheduling and Cancellation
 *
 * Scheduling and cancellation of coroutines using `kotlinx.coroutines`.
 *
 * Cooperative scheduling - coroutines yield manually.
 *
 * Expensive on threads.
 */
object SchedulingAndCancellation {
  private const val SEPARATOR = "================================="
  private val logger = KotlinLogging.logger("learning Coroutines")

  private suspend fun workingHard() {
    logger.info { "Working" }
    // CPU-intensive computation
    while (true) {
      // do some hard code
    }
    delay(100L)
    logger.info { "work done" }
  }

  private suspend fun takeABreak() {
    logger.info { "taking a break" }
    delay(1000L)
    logger.info { "break done" }
  }

  // coroutine in single thread
  @OptIn(ExperimentalCoroutinesApi::class)
  suspend fun workHardRoutineParallelOne() {
    // thread pool in top where coroutines are run
    // native thread pool for coroutines
    // force 2 threads
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)

    // ith parallelism 1, will never get chance to run the second launch
    coroutineScope {
      launch(dispatcher) { workingHard() }
      launch(dispatcher) { takeABreak() }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  suspend fun workHardRoutineParallelTwo() {
    // thread pool in top where coroutines are run
    // native thread pool for coroutines
    // force 2 threads
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(2)

    coroutineScope {
      // both are launched in parallel
      launch(dispatcher) { workingHard() }
      launch(dispatcher) { takeABreak() }
    }
  }

  // for manually yield, we need a suspending point
  internal suspend fun workingNicely() {
    logger.info { "working..." }
    // CPU-intensive computation
    while (true) {
      delay(1000L) // give a change for the dispatcher to run another coroutine
    }
    delay(100L)
    logger.info { "work done" }
  }

  // even though we have 1 thread with limited parallelism of 1, we can execute both at same time
  // because of the delay inside the while(true)
  @OptIn(ExperimentalCoroutinesApi::class)
  suspend fun workNicelyRoutine() {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)

    coroutineScope {
      launch(dispatcher) { workingNicely() }
      launch(dispatcher) { takeABreak() }
    }
  }

  // "normal code" = short code or have yielding points / yielding coroutines
  // such as the example above
  val simpleDispatcher = Dispatchers.Default

  // blocking code (e.g. DB connections, long-running computations) / IO-bound code
  // schedule the reading or writing ot a database, for instance, to speed-up de UX
  val blockingDispatcher = Dispatchers.IO

  // built on top of an execution context
  // wrap the executor service as a coroutine dispatcher
  // on top of your own thread pool
  val customDispatcher = Executors.newFixedThreadPool(8).asCoroutineDispatcher()

  // cancellation
  private suspend fun forgettingFriendBirthdayRoutine() {
    coroutineScope {
      // 2 coroutines
      val workingJob = launch { workingNicely() }
      launch {
        // after 2s I remember I have a birthday today
        delay(2000L)

        // sends a SIGNAL to the coroutine to cancel, cancellation happens at first yielding
        // point
        // without yielding point it won't be cancelled!
        workingJob.cancel()

        // you are sure that the coroutine has been cancelled / stopped or finished
        workingJob.join()
        logger.info { "forgot my friend's birthday! buying a present now!" }
      }
    }
  }

  // if a coroutine doesn't yield, it can't be cancelled
  suspend fun forgettingFriendBirthdayRoutineNonCancelable() {
    coroutineScope {
      val workingJob = launch { workingHard() }
      launch {
        delay(2000L)
        logger.info { "trying to stop working..." }

        // cancellation happens at first yielding point (NEVER)
        workingJob.cancel()
        // semantically blocks the coroutine
        workingJob.join()

        logger.info { "forgot my friend's birthday! buying a present now!" }
      }
    }
  }

  private suspend fun forgettingUrgentMeeting() {
    coroutineScope {
      val workingJob = launch { workingNicely() }
      launch {
        delay(2000L)
        // cancels the job and waits for its completion
        workingJob.cancelAndJoin()
        logger.info { "forgot I had an urgent meeting!" }
      }
    }
  }

  // cancel execution of a coroutine is because its execution time has exceeded some timeout
  // withTimeout throws an exception, but instead we can use withTimeoutOrNull
  private suspend fun sleepingRoutine(): Unit =
    withTimeoutOrNull(1300L) {
      repeat(1000) { i ->
        logger.info { "sleeping $i..." }
        delay(500L)
      }
      logger.info { "wake up rested" } // will get cancelled before it produces this result
    } ?: logger.info { "wake up exhausted" }

  context(CoroutineScope)
  private suspend fun lazilyWorking(): Long = measureTimeMillis {
    val partTimeJob =
      async(start = CoroutineStart.LAZY) {
        logger.info { "working on my first job..." }
        delay(1000L)
        10
      }
    val partTimeJobTwo =
      async(start = CoroutineStart.LAZY) {
        logger.info { "working on my second job..." }
        delay(1000L)
        15
      }
    // some computation
    partTimeJob.start() // start the first one
    partTimeJobTwo.start() // start the second one
    val sum = partTimeJob.await() + partTimeJobTwo.await()
    logger.info { "earned $sum dollars" }
  }

  // add -Dkotlinx.coroutines.debug to VM options
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // workHardRoutineParallelOne()
      // workHardRoutineParallelTwo()
      // workNicelyRoutine()
      // logger.info { SEPARATOR }

      forgettingFriendBirthdayRoutine()
      logger.info { SEPARATOR }

      // forgettingFriendBirthdayRoutineNonCancelable()
      // logger.info { SEPARATOR }

      forgettingUrgentMeeting()
      logger.info { SEPARATOR }

      sleepingRoutine()
      logger.info { SEPARATOR }

      val workTime = lazilyWorking()
      logger.info { "completed in $workTime ms" }
      logger.info { SEPARATOR }
    }
  }
}
