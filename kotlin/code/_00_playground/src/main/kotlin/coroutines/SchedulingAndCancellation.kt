@file:Suppress("Unused", "MagicNumber")

package coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.asCoroutineDispatcher

import java.util.concurrent.Executors

// From RockTheJVM video series in YT
// Kotlin Coroutines Tutorial, Part 2: Cooperative Scheduling, Cancellation, Coroutine Context

// cooperative scheduling - coroutines yield manually
// expensive on threads

suspend fun workingHard() {
    logger.info { "Working" }
    // CPU-intensive computation
    while (true) {
        // do some hard code
    }
    delay(100L)
    logger.info { "Work done" }
}

suspend fun takeABreak() {
    logger.info { "Taking a break" }
    delay(1000L)
    logger.info { "Break done" }
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
        launch(dispatcher) {
            workingHard()
        }
        launch(dispatcher) {
            takeABreak()
        }
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
        launch(dispatcher) {
            workingHard()
        }
        launch(dispatcher) {
            takeABreak()
        }
    }
}

// for manually yield, we need a suspending point
suspend fun workingNicely() {
    logger.info { "Working" }
    // CPU-intensive computation
    while (true) {
        delay(1000L) // give a change for the dispatcher to run another coroutine
    }
    delay(100L)
    logger.info { "Work done" }
}

// even though we have 1 thread with limited parallelism of 1, we can execute both at same time
// because of the delay inside the while(true)
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun workNicelyRoutine() {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)

    coroutineScope {
        launch(dispatcher) {
            workingNicely()
        }
        launch(dispatcher) {
            takeABreak()
        }
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
suspend fun forgettingFriendBirthdayRoutine() {
    coroutineScope {
        // 2 coroutines
        val workingJob = launch { workingNicely() }
        launch {
            // after 2s I remember I have a birthday today
            delay(2000L)

            // sends a SIGNAL to the coroutine to cancel, cancellation happens at first yielding point
            // without yielding point it won't be cancelled!
            workingJob.cancel()

            // you are sure that the coroutine has been cancelled / stopped or finished
            workingJob.join()
            logger.info { "I forgot my friend's birthday! Buying a present now!"}
        }
    }
}

// if a coroutine doesn't yield, it can't be cancelled
suspend fun forgettingFriendBirthdayRoutineNonCancelable() {
    coroutineScope {
        val workingJob = launch { workingHard() }
        launch {
            delay(2000L)
            logger.info { "Trying to stop working..."}

            // cancellation happens at first yielding point (NEVER)
            workingJob.cancel()
            // semantically blocks the coroutine
            workingJob.join()

            logger.info { "I forgot my friend's birthday! Buying a present now!"}
        }
    }
}

// add -Dkotlinx.coroutines.debug to VM options
suspend fun main() {
    //workHardRoutineParallelOne()
    //workHardRoutineParallelTwo()
    //workNicelyRoutine()
    logger.info { SEPARATOR }

    forgettingFriendBirthdayRoutine()
    logger.info { SEPARATOR }

    //forgettingFriendBirthdayRoutineNonCancelable()
    logger.info { SEPARATOR }
}
