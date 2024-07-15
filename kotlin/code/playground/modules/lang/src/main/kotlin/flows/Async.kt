package flows

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// 1. Flows - Prologue

object Async {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // Multiple values can be represented in Kotlin using collections. For example,
      // we can have a simple function that returns a List of three numbers and then print
      // them all using forEach:
      fun numbers(): List<Int> = listOf(1, 2, 3)

      numbers().forEach { value -> println(value) }
      println("-------------------")

      // If we are computing the numbers with some CPU-consuming blocking code then we can
      // represent the numbers using a Sequence

      // Sequence type represents lazily evaluated collections
      fun consumingNumbers(): Sequence<Int> = sequence {
        for (i in 1..10) {
          Thread.sleep(100) // pretend we are computing it
          yield(i) // yield next value
        }
      }

      // The sequence scope has restricted suspension, meaning we can only use the members defined
      // in the sequence scope. E.g. we cannot do `delay(100)` inside the sequence scope

      // This code the same numbers, but it waits 100ms before printing each one.
      consumingNumbers().forEach { println(it) }
      println("-------------------")

      // We can also use the `take` function to limit the number of elements
      consumingNumbers().take(5).forEach { println(it) }
      println("-------------------")

      // However, this computation blocks the main thread that is running the code.
      // When these values are computed by asynchronous code we can mark the simple function
      // with a suspend modifier, so that it can perform its work without blocking and
      // return the result as a list:

      suspend fun consumingNumbersSuspend(): List<Int> {
        delay(250) // pretend we are doing something asynchronous here
        return listOf(1, 2, 3)
      }

      consumingNumbersSuspend().forEach { println(it) }
      println("-------------------")

      // Using the List<Int> result type, means we can only return all the values at once.
      // To represent the stream of values that are being computed asynchronously, we can use a
      // Flow<Int> type just like we would use a Sequence<Int> type for synchronously computed
      // values:
      fun numbersFlow(): Flow<Int> = flow { // flow builder
        for (i in 1..3) {
          delay(100) // pretend we are doing something useful here
          emit(i) // emit next value
        }
      }

      // Launch a concurrent coroutine to check if the main thread is blocked
      launch {
        for (k in 1..3) {
          println("I'm not blocked $k")
          delay(100)
        }
      }

      // Collect the flow
      numbersFlow().collect { println(it) }
      println("-------------------")

      // This code waits 100ms before printing each number without blocking the main thread.
      // This is verified by printing "I'm not blocked" every 100ms from a separate coroutine
      // that is running in the main thread

      // We can replace delay with Thread.sleep in the body of our flow { ... } and
      // see that the main thread is blocked in this case.
      fun numbersFlowBlocking(): Flow<Int> = flow { // flow builder
        for (i in 1..3) {
          Thread.sleep(100) // pretend we are doing something useful here
          emit(i) // emit next value
        }
      }

      // Launch a concurrent coroutine to check if the main thread is blocked
      launch {
        for (k in 1..3) {
          println("I'm blocked... $k")
          delay(100)
        }
      }

      // Collect the flow
      numbersFlowBlocking().collect { println(it) }
    }
  }
}
