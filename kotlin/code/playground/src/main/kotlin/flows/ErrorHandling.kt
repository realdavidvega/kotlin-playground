package flows

import arrow.core.raise.catch
import flows.Flows.andrewGarfield
import flows.Flows.benAffleck
import flows.Flows.ezraMiller
import flows.Flows.galGadot
import flows.Flows.henryCavill
import flows.Flows.jasonMomoa
import flows.Flows.rayFisher
import flows.Flows.tobeyMaguire
import flows.Flows.tomHolland
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.runBlocking
import java.util.*

// 8. Flows and Coroutines - Part. II - Error handling

object ErrorHandling {

  interface ActorRepository {
    suspend fun findJLAActors(): Flow<Flows.Actor>
  }

  // default implementation we can use
  val actorRepository: ActorRepository =
    object : ActorRepository {
      var retries = 0
      override suspend fun findJLAActors(): Flow<Flows.Actor> = flow {
        emit(henryCavill)
        emit(galGadot)
        emit(ezraMiller)
        if (retries == 0) {
          retries++
          throw RuntimeException("Oooops")
        }
        emit(rayFisher)
        emit(benAffleck)
        emit(jasonMomoa)
      }
    }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // The Kotlin Coroutines library provides a function to handle the case of an empty flow:
      // the onEmpty function

      // It's a good choice for emitting some default value for an empty flow
      val actorsEmptyFlow =
        flow<Flows.Actor> { delay(1000) }
          .onEmpty {
            println("The flow is empty, adding some actors")
            emit(henryCavill)
            emit(benAffleck)
          }
          .collect { println(it) }

      println("-------------------")

      // Creating an empty flow using a dedicated builder called emptyFlow is possible.
      // The emptyFlow function returns a flow that emits no value
      val actorsEmptyFlow_v2 =
        emptyFlow<Flows.Actor>()
          .onStart { delay(1000) }
          .onEmpty {
            println("The flow is empty, adding some actors")
            emit(henryCavill)
            emit(benAffleck)
          }
          .collect { println(it) }

      println("-------------------")

      // Another typical case is when exceptions arise during the execution of a flow.
      // First, let’s see what happens if we don’t use any recovery mechanism
      val spiderMenActorsFlowWithException =
        catch({
          flow {
            emit(tobeyMaguire)
            emit(andrewGarfield)
            throw RuntimeException("An exception occurred")
            emit(tomHolland) // Not executed
          }
            .onStart { println("The Spider Men flow is starting") }
            .onCompletion { println("The Spider Men flow is completed") }
            .collect { println(it) }
        }) { e ->
          println("Exception: $e")
        }

      println("-------------------")

      // First, an exception in the flow execution breaks it and avoids the emission of the values
      // after the exception. The coroutine executing the suspending lambda function passed to
      // the collect function is canceled by the exception that will bubble up to the context
      // calling the collect function. Second, the onCompletion function is called even if an
      // exception is thrown during the execution of the flow. So, the onCompletion function
      // is called when the flow is completed, whether an exception is thrown or not.
      // We can think about it as a 'finally' block.

      // The library provides a catch method that we can chain to the flow to handle exceptions
      // fun <T> Flow<T>.catch(action: suspend FlowCollector<T>.(cause: Throwable) -> Unit): Flow<T> = TODO()

      // It takes a lambda that is called when an exception is thrown during the execution of the flow
      val spiderMenActorsFlowWithException_v3 =
        catch({
          flow {
            emit(tobeyMaguire)
            emit(andrewGarfield)
            throw RuntimeException("An exception occurred")
            emit(tomHolland) // Not executed
          }
            .catch { ex -> emit(tomHolland) } // capture the exception and emit the value
            .onStart { println("The Spider Men flow is starting") }
            .onCompletion { println("The Spider Men flow is completed") }
            .collect { println(it) }
        }) { e ->
          println("Exception: $e")
        }

      println("-------------------")

      // Another essential feature of the catch function is that it catches all the exceptions
      // thrown during the executions of the transformations chained to the flow before it
      val spiderMenNames =
        flow {
          emit(tobeyMaguire)
          emit(andrewGarfield)
          emit(tomHolland)
        }
          .map { // map each value
            if (it.firstName == Flows.FirstName("Tom")) {
              throw RuntimeException("Ooops")
            } else {
              "${it.firstName.value} ${it.lastName.value}"
            }
          }.catch { ex -> emit("Tom Holland") }
          .map { it.uppercase(Locale.getDefault()) }
          .collect { println(it) }

      println("-------------------")

      // But, if we move the throwing of the exception after the catch function, nothing will catch
      // the exception thrown by the second map function, the flow will be canceled, and
      // the exception will bubble up
      val spiderMenNames_v2 =
        catch({
          flow {
            emit(tobeyMaguire)
            emit(andrewGarfield)
            emit(tomHolland)
          }
            .map { "${it.firstName.value} ${it.lastName.value}" }
            .catch { ex -> emit("Tom Holland") }
            .map {
              if (it == "Tom Holland") {
                throw RuntimeException("Oooops")
              } else {
                it.uppercase(Locale.getDefault())
              }
            }
            .collect { println(it) }
        }) { e ->
          println("Exception: $e")
        }

      println("-------------------")

      // So, we can think about the catch function as a catch block that handles all the exceptions
      // thrown before it in the chain. For this reason, the catch function can’t catch the exceptions
      // thrown by the collect function since it’s the terminal operation of the flow.
      val spiderMenActorsFlowWithException_v4 =
        catch({
          flow {
            emit(tobeyMaguire)
            emit(andrewGarfield)
            emit(tomHolland)
          }
            .catch { ex -> println("I caught an exception!") }
            .onStart { println("The Spider Men flow is starting") }
            .onCompletion { println("The Spider Men flow is completed") }
            .collect {
              if (true) throw RuntimeException("Oooops") // this will break the flow
              println(it)
            }
        }) { e ->
          println("Exception: $e")
        }

      println("-------------------")

      // The only way we can prevent this case is to move the collect logic into a dedicated
      // onEach function and put a catch in the chain after the onEach function
      val spiderMenActorsFlowWithException_v5 =
        flow {
          emit(tobeyMaguire)
          emit(andrewGarfield)
          emit(tomHolland)
        }
          .onEach {
            if (true) throw RuntimeException("Oooops")
            println(it)
          }
          .catch { ex -> println("I caught an exception!") }
          .onStart { println("The Spider Men flow is starting") }
          .onCompletion { println("The Spider Men flow is completed") }
          .collect()

      println("-------------------")

      // Also, if an exception is thrown in the catch function, it will be needed to be caught again
      val spiderMenActorsFlowWithException_v6 =
        catch(
          {
            flow {
              emit(tobeyMaguire)
              emit(andrewGarfield)
              emit(tomHolland)
              throw RuntimeException("Oooops")
            }
              .catch { ex ->
                println("I caught an exception!")
                throw RuntimeException("Another oooops")
              }
              .catch { ex -> println("I caught another exception!") }
              .collect()
          }) {
          println("Exception: $it")
        }

      println("-------------------")

      // What if we want to embrace that an operation can fail now and then, and we want to retry it
      // The Kotlin Coroutines library provides a function to retry the execution of a flow in
      // case of an exception: the retry function.
      // Kotlin Coroutines Library
      //  public fun <T> Flow<T>.retry(
      //    retries: Long = Long.MAX_VALUE,
      //    predicate: suspend (cause: Throwable) -> Boolean = { true }
      //  ): Flow<T>

      // Executing the findJLAActors function will throw an exception the first time it’s called.
      // The second time, it will emit all the actors playing in the “Zack Snyder’s Justice League”
      // movie. The above example mimics a temporary network glitch.
      catch({
        actorRepository
          .findJLAActors()
          .collect { println(it) }
      }) {
        println("An error occurred during the execution: $it")
      }

      println("-------------------")

      // We can now use the retry function to retry the execution of the findJLAActors
      // function and print all the actors playing in the movie
      actorRepository
        .findJLAActors()
        .retry(2)
        .collect { println(it) }

      println("-------------------")

      // However, in such cases, waiting between the retries to resolve the glitch is familiar and
      // good practice. We can add a delay in the lambda passed to the retry function
      actorRepository
        .findJLAActors()
        .onStart { println("The actors flow is starting") }
        .retry(2) { ex ->
          println("An exception occurred: '${ex.message}', retrying...")
          delay(1000)
          true
        }
        .onCompletion { println("The actors flow is completed") }
        .collect { println(it) }

      println("-------------------")

      // Having the cause of the exception in input, we can always decide whether to retry
      // based on the exception type.

      // In a real-world scenario, we will use a more sophisticated back-off policy and
      // avoid retrying multiple times using the same interval.
      // public fun <T> Flow<T>.retryWhen(
      //   predicate: suspend FlowCollector<T>.(cause: Throwable, attempt: Long) -> Boolean
      // ): Flow<T> = TODO()

      // We can rewrite the previous example using the retryWhen function as follows,
      // retrying with an increasing delay between the attempts
      actorRepository
        .findJLAActors()
        .onStart { println("The actors flow is starting") }
        .retryWhen { cause, attempt ->
          println("An exception occurred: '${cause.message}', retry number $attempt...")
          delay(attempt * 1000)
          true
        }
        .onCompletion { println("The actors flow is completed") }
        .collect { println(it) }
    }
  }
}
