@file:Suppress("Unused", "TooManyFunctions", "MagicNumber")

package coroutines

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Structured Concurrency
 *
 * Structured concurrency with coroutines, using `kotlinx.coroutines`.
 *
 * coroutine = lightweight "thread" easier to start, schedule, stop...
 *
 * best for parallel + concurrent apps
 */
object StructuredConcurrency {
  private const val SEPARATOR = "================================="
  private val logger = KotlinLogging.logger("Learning Coroutines")

  // non-blocking, running on other thread
  private suspend fun bathTime() {
    // Continuation = data structure stores all local context
    logger.info { "Going to the bathroom" }

    // suspend/"blocks" the computation / semantic blocking
    delay(500L)

    // Continuation restored here
    logger.info { "Bath done, exiting" }
  }

  // structured concurrency
  private suspend fun boilingWater() {
    logger.info { "Boiling water" }
    delay(1000L)
    logger.info { "Water boiled" }
  }

  private suspend fun sequentialMorningRoutine() {
    // construct / wrapper over suspend functions to start a "context" for coroutines
    coroutineScope {
      bathTime()
      // add more code, including suspending functions (and isolated)
      // parallel code here, all needs to finish before the scope is closed
    }

    // executed one before the other
    // powerful: sharing context, cancellation, parenting-child features
    coroutineScope { boilingWater() }
  }

  private suspend fun concurrentMorningRoutine() {
    // parent construct / function
    coroutineScope {
      // new coroutines in parallel
      // new Thread(() => ...).start / runnable
      launch { bathTime() }

      // this coroutine is a CHILD of the coroutineScope
      launch { boilingWater() }
    }
  }

  // without parent need
  @OptIn(DelicateCoroutinesApi::class)
  suspend fun noStructConcurrencyMorningRoutine() {
    // but no trivial error handling
    GlobalScope.launch { bathTime() }
    GlobalScope.launch { boilingWater() }
  }

  private suspend fun makeCoffee() {
    logger.info { "Starting to make coffee" }
    delay(500L)
    logger.info { "Done with coffee" }
  }

  /*  plan coroutines:

     take a bath
     start the boiling water

     after both are done => drink my coffee
  */
  private suspend fun morningRoutineWithCoffee() {
    coroutineScope {
      // in parallel
      val bathTimeJob: Job = launch { bathTime() }
      val boilingWaterJob: Job = launch { boilingWater() }
      bathTimeJob.join() // block
      boilingWaterJob.join() // block
      launch { makeCoffee() }
    }
  }

  // structured concurrency
  private suspend fun morningRoutineWithCoffeeStructured() {
    coroutineScope {
      // end when all jobs done
      coroutineScope {
        // parallel jobs
        launch { bathTime() }
        launch { boilingWater() }
      }
      // both coroutines are done
      launch { makeCoffee() }
    }
  }

  // return values from coroutines
  private suspend fun preparingJavaCoffee(): String {
    logger.info { "Starting to make coffee" }
    delay(500L)
    logger.info { "Done with coffee" }
    return "Java coffee"
  }

  private suspend fun toastingBread(): String {
    logger.info { "Starting to make breakfast" }
    delay(1000L)
    logger.info { "Toast is out!" }
    return "Toasted bread"
  }

  private suspend fun prepareBreakfast() {
    coroutineScope {
      val coffee: Deferred<String> = async {
        preparingJavaCoffee()
      } // Deferred = analogous to the Future[T]
      val toast: Deferred<String> = async { toastingBread() }
      // semantic blocking bcz happens in coroutineScope
      val finalCoffee = coffee.await()
      val finalToast = toast.await()
      logger.info { "I'm eating $finalToast and drinking $finalCoffee" }
    }
  }

  private suspend fun prepareBreakfastAwaitAll() {
    coroutineScope {
      val coffee: Deferred<String> = async { preparingJavaCoffee() }
      val toast: Deferred<String> = async { toastingBread() }
      // semantic blocking
      val breakfast = awaitAll(coffee, toast)
      logger.info { "I'm eating ${breakfast[0]} and drinking ${breakfast[1]}" }
    }
  }

  // add -Dkotlinx.coroutines.debug to VM options

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // won't work without suspend
      bathTime()
      logger.info { SEPARATOR }

      sequentialMorningRoutine()
      logger.info { SEPARATOR }

      concurrentMorningRoutine()
      logger.info { SEPARATOR }

      noStructConcurrencyMorningRoutine()

      // the main thread exists before it finishes, so we need to block the main thread in the main
      Thread.sleep(2000)
      logger.info { SEPARATOR }

      morningRoutineWithCoffee()
      logger.info { SEPARATOR }

      morningRoutineWithCoffeeStructured()
      logger.info { SEPARATOR }

      prepareBreakfast()
      logger.info { SEPARATOR }

      prepareBreakfastAwaitAll()
      logger.info { SEPARATOR }
    }
  }
}
