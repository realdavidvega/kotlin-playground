import arrow.core.Either
import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.Schedule
import arrow.fx.coroutines.retry
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

// Arrow Fx Coroutines
// Schedule
// With Schedule we can retry or repeat effects based on a policy. It is highly composable.
// Here is an example of a complex policy:

class Talk(id: String)
suspend fun fetchTalk(id: String): Talk = Talk(id)

@OptIn(ExperimentalTime::class)
fun policy() = Schedule.exponential<Throwable>(10.milliseconds)
    .whileOutput { it < 300.milliseconds }
    .zipRight(Schedule.collect())

suspend fun loadTalk(id: String): Talk =
    policy().retry {
        fetchTalk(id)
    }

// With this, if the fetchTalk(id) request failed, it would be retried following the given policy.
// That is an exponential retry starting on 10 millis that duplicates every time (default factor is 2.0), and
// while it stays under 300 milliseconds. Then zipRight basically discards the previous Schedule results and only
// keeps the ones collected by Schedule.collect(), which in this case it will be a List with a single result since
// try can only succeed once.

// Note that if the policy is complete and the effect keeps failing, that'll rethrow the exception, so
// that's something to take into account when dealing with effects that could potentially fail indefinitely.

//////////////////////

suspend fun loadUsersActive(): Int = throw RuntimeException("Boom!")

@OptIn(ExperimentalTime::class)
fun usersPolicy() = Schedule.exponential<Throwable>(10.milliseconds)
    .whileOutput { it < 300.milliseconds }
    .zipRight(Schedule.collect())

// it retries a request to loadUsersActive() following the provided policy. Update the counter every time
// the request is retried, and finally return the counter value, so we can automatically validate.
// P.S: Keep an eye on those errors, so it doesn't throw if policy never gets fulfilled.
// count.update { it + 1 } updates the counter on each repetition.
// Uses Either.catch {} to be covered from throwing on the final retry, if we exceed the policy. Try-catch would
// also be valid.
suspend fun loadUsersActiveWithPolicy(): Int {
    val count = Atomic(0)

    Either.catch {
        usersPolicy().retry {
            count.update { it + 1 }
            loadUsersActive()
        }
    }

    return count.get()
}

suspend fun main() {
    val count = loadUsersActiveWithPolicy()
    println(count)
}
