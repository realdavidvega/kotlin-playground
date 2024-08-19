package resilience

import arrow.continuations.SuspendApp
import arrow.core.raise.catch
import arrow.resilience.Schedule
import arrow.resilience.retryOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 1. Retry and Repeat Patterns A common demand when working with actions is to retry or repeat them
 *    when (adverse) certain circumstances happen. Usually, the retrial or repetition does not occur
 *    immediately; instead, it is done based on a policy. For instance, when fetching content from a
 *    network request, we may want to retry it when it fails using an exponential backoff algorithm
 *    for a maximum of 15 seconds or 5 attempts, whatever happens first.
 */
object RetryAndRepeat {

  data object MyCustomException : RuntimeException("")

  private val logger = KotlinLogging.logger("Retry and Repeat Patterns")

  // Retry and Repeat Patterns

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    catch({
      // The Retry and Repeat Pattern addresses the need to retry or repeat actions under certain
      // circumstances, typically based on a defined policy. For example, when making network
      // requests,
      // we might want to retry failed requests using an exponential backoff algorithm for a maximum
      // of
      // 15 seconds or 5 attempts, whichever comes first.

      // Schedule allows defining and composing powerful yet simple policies.
      val fiveAttempts = Schedule.recurs<Unit>(5)
      val fifteenSeconds = Schedule.linear<Unit>(15.seconds)

      // And allows combining them. If one of the policies is done, the other one is not executed.
      val retryPolicy = fiveAttempts and fifteenSeconds

      // There are two steps involved in using Schedule:
      // 1. Constructing a policy specifying the amount and delay in repetition.
      // 2. Running this schedule with a specified action. There are two ways to do so:
      // - retry executes the action once, and if it fails, it is reattempted based on the
      // scheduling
      // policy. It stops when the action succeeds or when the policy determines it should not be
      // reattempted again.
      // - repeat executes the action, and if it succeeds, keeps executing it again based on the
      // scheduling policy passed as an argument. It stops if the action fails or the policy
      // determines it should not be executed again. It returns the last internal state of the
      // scheduling policy or the error that happened running the action.

      // If we call repeat, the same action is performed ten times
      suspend fun repeat() {
        var counter = 0
        fiveAttempts.repeat {
          println(counter)
          counter++
        }
      }

      repeat()
      println("-------------------")

      // It's important to notice that a Schedule, in fact, describes how a suspend function
      // should either retry or repeat. That's why we need to call it from a suspend function or a
      // coroutine.

      // We can create our own custom schedule, with maxRetries and backoff parameters
      // MaxRetries is the maximum number of retries
      // Backoff is the delay between retries, with a jitter of 75% and 125%
      // Jitter means that the delay is multiplied by a random number between 0.75 and 1.25
      // It will only be used if the exception is a MyCustomException
      fun customSchedule(
        maxRetries: Long = 3L,
        backoff: Duration = 2.seconds,
      ): Schedule<Throwable, Long> =
        Schedule.recurs<Throwable>(maxRetries)
          .zipLeft(Schedule.exponential<Throwable>(backoff).jittered(0.75, 1.25))
          .doWhile { throwable, _ -> throwable is MyCustomException }

      // We can use the schedule, retrying a given block of code which returns an A
      // We retry only if MyCustomException is thrown
      suspend fun <A> customRetry(block: suspend () -> A): A =
        customSchedule()
          .log { e, retriesSoFar ->
            logger.error { "Retry failed with $e after $retriesSoFar retries..." }
          }
          .retryOrElse({ block() }) { error, retries ->
            when (error) {
              is MyCustomException ->
                logger.error { "All retries failed so far, after $retries retries" }
              else -> logger.error { "Retry failed with $error" }
            }
            throw error
          }

      customRetry { throw MyCustomException }
    }) {
      println(it)
    }
  }
}
