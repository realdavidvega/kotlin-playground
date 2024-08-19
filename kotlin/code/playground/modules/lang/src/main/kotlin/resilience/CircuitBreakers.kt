package resilience

import arrow.core.Either
import arrow.core.raise.catch
import arrow.resilience.CircuitBreaker
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Circuit Breakers
 *
 * When a service is overloaded, additional interaction may only worsen its overloaded state. This
 * is especially true when combined with retry mechanisms such as Schedule. Sometimes, simply using
 * a back-off retry policy might not be sufficient during peak traffic. To prevent such overloaded
 * resources from overloading, a circuit breaker protects the service by failing fast. This helps us
 * achieve stability and prevents cascading failures in distributed systems.
 */
object CircuitBreakers {

  // max number of consecutive failures before moving to open
  private const val MAX_FAILURES = 3

  private val countCircuitBreaker =
    CircuitBreaker(
      openingStrategy = CircuitBreaker.OpeningStrategy.Count(maxFailures = MAX_FAILURES),
      resetTimeout = 1.seconds, // time to wait before moving to half open
      exponentialBackoffFactor = 1.25, // multiplier for exponential backoff
      maxResetTimeout = 10.seconds, // maximum timeout during half open
    )

  /** Circuit breaker protocol, may be in one of three states. */

  /**
   * Closed This is the state in which the circuit breaker starts. Requests are made normally in
   * this state: When an exception occurs, it increments the failure counter. When the failure
   * counter reaches the given maxFailures threshold, the breaker moves to the Open state. A
   * successful request will reset the failure counter to zero.
   */

  /**
   * Open In this state, the circuit breaker short-circuits/fails-fast all requests. This is done by
   * throwing the ExecutionRejected exception. If a request is made after the configured
   * resetTimeout, the breaker moves to the Half Open state, allowing one request to go through as a
   * test.
   */

  /**
   * Half Open The circuit breaker is in this state while allowing a request to go through as a test
   * request. All other requests made while test request` is still running short-circuit/fail-fast.
   * If the test request succeeds, the circuit breaker is tripped back into Closed, with the
   * resetTimeout and the failures count also reset to initial values. If the test request fails,
   * the circuit breaker moves back to Open, and the resetTimeout is multiplied by the
   * exponentialBackoffFactor up to the configured maxResetTimeout.
   */

  /**
   * Strategies for opening the circuit breaker:
   * 1. Count: This strategy sets a maximum number of failures. Once this threshold is reached, the
   *    circuit breaker moves to Open. Note that every time a request succeeds, the counter is set
   *    back to zero; the circuit breaker only moves to Open when the maximum number of failures
   *    happen consecutively.
   * 2. Sliding Window. This strategy counts the number of failures within a given time window.
   *    Unlike the Count approach, the circuit breaker will only move to Open if the number of
   *    failing requests tracked within the given period exceeds the threshold. As the time window
   *    slides, the failures out of the window limits are ignored.
   */
  fun slidingWindowWithPrints(
    openingStrategy: CircuitBreaker.OpeningStrategy =
      CircuitBreaker.OpeningStrategy.SlidingWindow(
        timeSource = TimeSource.Monotonic,
        windowDuration = 5.seconds,
        maxFailures = MAX_FAILURES,
      ),
    resetTimeout: Duration = 2.seconds,
    exponentialBackoffFactor: Double = 1.25,
    maxResetTimeout: Duration = 10.seconds,
  ) =
    CircuitBreaker(
      openingStrategy = openingStrategy,
      resetTimeout = resetTimeout,
      exponentialBackoffFactor = exponentialBackoffFactor,
      maxResetTimeout = maxResetTimeout,
      onOpen = { println("Circuit breaker is open!") },
      onClosed = { println("Circuit breaker is closed!") },
      onHalfOpen = { println("Circuit breaker is half open!") },
      onRejected = { println("Circuit breaker is rejected!") },
    )

  /** Singleton service to get fruits */
  class FruitsService {
    var counter = 0

    /**
     * Additionally, it's important to note that if several (concurrent) threads access the same
     * service, hey should be protected by the same circuit breaker. That is, not circuit breakers
     * created with the same parameters, but literally the same instance.
     */
    val myCircuitBreaker = slidingWindowWithPrints()

    /** Simulate four consecutive failures */
    suspend fun getFruitsFailFour(): List<String> {
      // fail four times
      return if (counter < 4) {
        counter++
        throw RuntimeException("BOOM!")
      } else {
        counter = 0 // reset counter
        listOf("apple", "banana", "cherry")
      }
    }

    suspend fun getFruitsWorking(): List<String> = listOf("apple", "banana", "cherry")
  }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val service = FruitsService()

      // normal operation
      countCircuitBreaker
        .protectOrThrow { "I am in Closed: ${countCircuitBreaker.state()}" }
        .also(::println)

      /**
       * Will protect the service from being overloaded by throwing the ExecutionRejected exception.
       */
      (0..4).forEach {
        Either.catch { countCircuitBreaker.protectOrThrow { service.getFruitsFailFour() } }
          .also(::println)
      }

      println("Service counter: ${service.counter}")

      countCircuitBreaker
        .protectEither {
          // will short circuit, meaning service will not be called and counter will not be
          // incremented
          // it will stay in Open state until reset timeout is reached
          // after reset timeout, will be in HalfOpen state and service will be called
          service.getFruitsFailFour()
        }
        .also { println("I am Open and short-circuit with ${it}. ${countCircuitBreaker.state()}") }

      println("Service counter: ${service.counter}")

      // simulate reset timeout
      println("Service recovering...").also { delay(2000) }

      // simulate test request success
      countCircuitBreaker
        .protectOrThrow {
          // if successful, will move to open
          service.getFruitsFailFour()
          "I am running it in half-open state: ${countCircuitBreaker.state()}"
        }
        .also(::println)
      println("I am back to normal state closed ${countCircuitBreaker.state()}")

      println("Service counter: ${service.counter}")

      // won't have any effect because nothing is wrong
      (0..4).forEach {
        Either.catch {
            countCircuitBreaker.protectOrThrow { service.getFruitsWorking() }
            println("All good!")
          }
          .also(::println)
      }

      countCircuitBreaker
        .protectOrThrow {
          service.getFruitsWorking()
          "I am still running it in closed state: ${countCircuitBreaker.state()}"
        }
        .also(::println)
    }
  }
}
