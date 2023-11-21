import arrow.fx.coroutines.ExitCase
import arrow.fx.coroutines.guaranteeCase
import arrow.fx.coroutines.parZip
import kotlinx.coroutines.*

// Arrow Fx Coroutines
// Cancellation
// In Arrow Fx Coroutines cancellation works the same way for all operators. If you cancel parent,
// children get cancelled.

// The library integrates seamlessly with KotlinX Coroutines structured concurrency, meaning you can call async {}
// over a CoroutineScope to create a job that will get cancelled whenever the scope is.

// For manual cancellation we can also keep a reference to the Deferred result of the async Coroutine builder,
// as we do in KotlinX Coroutines:

//suspend fun main() {
//    coroutineScope {
//        val deferred = async {
//            withContext(heavySuspendedTask())
//        }
//        deferred.cancel() // would cancel the heavy task.
//    }
//}

//////////////////////

var exitCaseA: ExitCase = ExitCase.Completed
var exitCaseB: ExitCase = ExitCase.Completed

suspend fun taskA(): Unit =
    guaranteeCase({ delay(1000) }) { ex -> exitCaseA = ex }
suspend fun taskB(): Unit =
    guaranteeCase({ delay(1000) }) { ex -> exitCaseB = ex }

// Uses a CoroutineScope to call the async Coroutine builder. It can be an inherited scope from the parent.
// Calls taskA and taskB in parallel using parZip. We can use async to launch the coroutine.
// Feel free to ignore the result of both tasks, it's not relevant for this exercise.
// Keeps a reference to the async result to cancels it later.
// Delay for 500ms, then cancel the async job. That should make both tasks also cancel by automatic propagation.
// Note that they are still running sine they take 1000ms.
// Keep the return line as is, so the test can validate the exit cases for both tasks.
// Note how both tasks use guaranteeCase to run a long suspended effect, and ensure that a finalizer function is
// always called after that no matter what. This finalizer function updates both ExitCase variables that the
// test uses to validate and only pass if both exit cases are Cancelled.
suspend fun someGuaranteeProgram(): Pair<ExitCase, ExitCase> {
    coroutineScope {
        val deferred = async {
            parZip(
                { taskA() },
                { taskB() }
            ) {
                    _, _ -> // do nothing
            }
        }
        delay(500)
        deferred.cancel()
    }

    return Pair(exitCaseA, exitCaseB)
}

suspend fun main() {
    val pair = someGuaranteeProgram()
    println(pair)
}
