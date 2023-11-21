import Errors.*
import arrow.core.Either
import arrow.core.computations.either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// Arrow Fx Coroutines
// withContext(ctx)

// There are times when we want to offload computations to a different CoroutineContext and then get back to the
// original one. This is common in Android, for example, where you want to offload tasks off the main thread and
// come back to it with the results.

// Arrow Fx Coroutines integrates perfectly with KotlinX Coroutines withContext. Note that it is suspended,
// so it can only be run under a controlled environment prepared for it.

suspend fun runnable() {
    val users = withContext(Dispatchers.IO) {
        //networkService.fetchUsers()
    }
    println(users)
}

//////////////////////

// it calls the sendTweet(Tweet) on the provided CoroutineContext.
suspend fun program(ctx: CoroutineContext): Either<Throwable, Unit> =
    withContext(ctx) {
        sendTweet(Tweet)
    }

suspend fun main() {
    program()
}
