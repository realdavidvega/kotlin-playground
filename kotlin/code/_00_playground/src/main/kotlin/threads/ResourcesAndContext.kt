@file:Suppress("MatchingDeclarationName", "MagicNumber")

package threads

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Desk : AutoCloseable {
  init {
    logger.info { "Starting to work on this desk" }
  }

  override fun close() {
    logger.info { "Cleaning up the desk" }
  }
}

suspend fun forgettingFriendBirthdayRoutineWithResource() {
  val desk = Desk()
  coroutineScope {
    val workingJob = launch {
      // use the resource here
      desk.use { _ -> // this resource will be closed upon completion of the coroutine
        workingNicely()
      }
    }

    // can also define your own "cleanup" code in case of completion
    workingJob.invokeOnCompletion { _: Throwable? -> // cancellation exception
      // can handle completion and cancellation differently, depending on the exception
      logger.info { "Make sure I talk to my colleagues that I'll be out for 30mins" }
    }

    launch {
      delay(2000L)
      workingJob.cancel()
      workingJob.join()
      logger.info { "I forgot my friend's birthday! Buying a present now!" }
    }
  }
}

// cancellation propagates to child coroutines
suspend fun drinkWater() {
  while (true) {
    logger.info { "Drinking water" }
    delay(1000L)
  }
}

suspend fun forgettingFriendBirthdayRoutineStayHydrated() {
  coroutineScope {
    // parent coroutine
    val workingJob = launch {
      // two child coroutines
      launch { workingNicely() }
      launch { drinkWater() }
    }
    launch {
      delay(2000L)
      workingJob.cancel() // will cancel both child coroutines
      workingJob.join()
      logger.info { "I forgot my friend's birthday! Buying a present now!" }
    }
  }
}

// coroutines context
// data structure that the coroutine can use the get started with
// for example the dispatcher / the coroutine name

suspend fun asynchronousGreeting() {
  coroutineScope {
    // giving it a name
    // combining properties, like the dispatcher, so we launch it on a particular dispatcher
    // these two = CoroutineContext (well it actually has a bigger structure)
    launch(CoroutineName("Greeting Coroutine") + Dispatchers.Default) {
      logger.info { "Hello, everyone!" }
    }
  }
}

// coroutine context can be inherited in child coroutines
suspend fun demoContextInheritance() {
  coroutineScope {
    launch(CoroutineName("Greeting Coroutine")) {
      logger.info { "[parent coroutine] Hello!" }
      launch { // coroutine context will be inherited here
        logger.info { "[child coroutine] Hi there!" }
      }
      delay(200)
      logger.info { "[parent coroutine] Hi again from parent!" }

      // we can overwrite some properties
      launch(CoroutineName("Child Greeting Coroutine")) { logger.info { "[child coroutine] Yo!" } }
    }
  }
}

// add -Dkotlinx.coroutines.debug to VM options
suspend fun main() {
  forgettingFriendBirthdayRoutineWithResource()
  logger.info { SEPARATOR }

  forgettingFriendBirthdayRoutineStayHydrated()
  logger.info { SEPARATOR }

  asynchronousGreeting()
  logger.info { SEPARATOR }

  demoContextInheritance()
  logger.info { SEPARATOR }
}
