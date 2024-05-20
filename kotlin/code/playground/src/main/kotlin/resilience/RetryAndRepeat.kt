package resilience

import arrow.resilience.Schedule
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

object RetryAndRepeat {

  // Retry Pattern

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // The Retry and Repeat Pattern addresses the need to retry or repeat actions under certain
      // circumstances, typically based on a defined policy. For example, when making network requests,
      // we might want to retry failed requests using an exponential backoff algorithm for a maximum of
      // 15 seconds or 5 attempts, whichever comes first.

      // Schedule allows defining and composing powerful yet simple policies.
      val fiveAttempts = Schedule.recurs<Unit>(5)
      val fifteenSeconds = Schedule.linear<Unit>(15.seconds)

      // And allows combining them. If one of the policies is done, the other one is not executed.
      val retryPolicy = fiveAttempts and fifteenSeconds

      // There are two steps involved in using Schedule:
      // 1. Constructing a policy specifying the amount and delay in repetition.
      // 2. Running this schedule with a specified action. There are two ways to do so:
      // - retry executes the action once, and if it fails, it is reattempted based on the scheduling
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
    }
  }
}
