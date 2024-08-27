package flows

import flows.Flows.benAffleck
import flows.Flows.ezraMiller
import flows.Flows.flowOf
import flows.Flows.galGadot
import flows.Flows.henryCavill
import flows.Flows.jasonMomoa
import flows.Flows.rayFisher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * (5) Flows - Coroutines
 */
object Coroutines {

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // Collect function is inherently synchronous despite being a suspending function.
      // No new coroutine is started under the hood
      val zackSnyderJusticeLeague: Flow<Flows.Actor> =
        flowOf(henryCavill, galGadot, ezraMiller, rayFisher, benAffleck, jasonMomoa)
      println("Before Zack Snyder's Justice League")
      zackSnyderJusticeLeague.collect { println(it) }
      println("After Zack Snyder's Justice League")

      println("-------------------")

      // To kick in asynchronous behavior, we must use the launch function from
      // the CoroutineScope interface
      val delayedJusticeLeague: Flow<Flows.Actor> = flow {
        delay(250)
        emit(henryCavill)
        delay(250)
        emit(galGadot)
        delay(250)
        emit(ezraMiller)
        delay(250)
        emit(rayFisher)
        delay(250)
        emit(benAffleck)
        delay(250)
        emit(jasonMomoa)
      }

      // Now, the flow is collected inside a dedicated coroutine spawned by the launch coroutine
      // builder, the program will not wait for the whole collection of the flow to complete before
      // printing the last string
      coroutineScope {
        println("Before Zack Snyder's Justice League")
        launch { delayedJusticeLeague.collect { println(it) } }
          .invokeOnCompletion { println("-------------------") }
        println("After Zack Snyder's Justice League")
      }

      // Executing the collection of the values of a flow in a separate coroutine is such a
      // typical pattern that the Kotlin Coroutines library provides a dedicated function to do
      // that: the launchIn function
      coroutineScope {
        println("Before Zack Snyder's Justice League V2")
        delayedJusticeLeague
          .onEach { println(it) }
          .launchIn(this)
          .invokeOnCompletion { println("-------------------") }
        println("After Zack Snyder's Justice League V2")
      }

      // Every suspending function must have a coroutine context, and suspending lambdas
      // used as input to the flows function is no exception. A flow uses internally the context
      // of the coroutine that calls the collect function.
      val delayedJusticeLeague_v2: Flow<Flows.Actor> =
        flow {
            println("${currentCoroutineContext()[CoroutineName]?.name} - In the flow")
            emit(henryCavill)
            emit(galGadot)
            emit(ezraMiller)
            emit(rayFisher)
            emit(benAffleck)
            emit(jasonMomoa)
          }
          .onEach { delay(250) }

      withContext(CoroutineName("Main")) {
        coroutineScope {
          println(
            "${currentCoroutineContext()[CoroutineName]?.name} - Before Zack Snyder's Justice League"
          )
          withContext(CoroutineName("Zack Snyder's Justice League")) {
            delayedJusticeLeague_v2.collect { println(it) }
          }
          println(
            "${currentCoroutineContext()[CoroutineName]?.name} - After Zack Snyder's Justice League"
          )
        }
      }

      println("-------------------")

      // Hence, we effectively changed the context of the coroutine that emits the values of the
      // flow. Whereas, if we don’t change the context, the context of the coroutine that emits
      // the values of the flow is the same as the context of the main coroutine
      withContext(CoroutineName("Main")) {
        coroutineScope {
          println(
            "${currentCoroutineContext()[CoroutineName]?.name} - Before Zack Snyder's Justice League"
          )
          delayedJusticeLeague_v2.collect { println(it) }
          println(
            "${currentCoroutineContext()[CoroutineName]?.name} - After Zack Snyder's Justice League"
          )
        }
      }

      println("-------------------")

      // Changing the context of the coroutine that executes the flow is so common that the Kotlin
      // Coroutines library provides a dedicated function: the flowOn function. The flowOn
      // function changes the coroutine context that emits the flow values
      withContext(CoroutineName("Main")) {
        coroutineScope {
          println(
            "${currentCoroutineContext()[CoroutineName]?.name} - Before Zack Snyder's Justice League"
          )
          delayedJusticeLeague_v2.flowOn(CoroutineName("Zack Snyder's Justice League")).collect {
            println(it)
          }
          println(
            "${currentCoroutineContext()[CoroutineName]?.name} - After Zack Snyder's Justice League"
          )
        }
      }

      // We can also use the flowOn function to change the dispatcher to execute the flow.
      // If it performs I/O operations, such as calling an external API or writing/reading
      // to/from a database, we can change the dispatcher to Dispatchers.IO
      val actorRepository: ErrorHandling.ActorRepository =
        object : ErrorHandling.ActorRepository {
          override suspend fun findJLAActors(): Flow<Flows.Actor> =
            flowOf(henryCavill, galGadot, ezraMiller, rayFisher, benAffleck, jasonMomoa).onEach {
              delay(250)
            }
        }
      actorRepository
        .findJLAActors()
        .flowOn(CoroutineName("Main") + Dispatchers.IO)
        .onEach { actor -> println("${currentCoroutineContext()[CoroutineName]?.name} - $actor") }
        .collect()
    }
  }
}
