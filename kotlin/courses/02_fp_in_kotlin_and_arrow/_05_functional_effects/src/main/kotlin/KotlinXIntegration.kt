import arrow.core.Either
import kotlinx.coroutines.*

// Arrow Fx Coroutines
// KotlinX Coroutines integration
// Many frameworks provide suspend entry points, like suspend fun main() in Kotlin, or suspend endpoint
// builders in Ktor. If the framework you are using provides an environment that is already suspend,
// you are sure it's prepared to be non-blocking.

object Users

object NetworkService {
    fun fetchUsers(): Users =
        TODO()
}

// We can call side effects here already.
suspend fun otherRunnable() {
    val networkService = NetworkService
    val users = networkService.fetchUsers()
    println(users)
}

// You'd be safe to call suspend functions from there, since the program runs on a Coroutine, hence you are covered.

// In case your platform doesn't support suspend out of the box, Arrow Fx Coroutines integrates perfectly
// with all the KotlinX Coroutines entry points: CoroutineScope.launch, CoroutineScope.async... etc.
// One example could be Android.

//fun onCreate() {
//    // ...
//    viewModelScope.async { networkService.fetchUsers() }
//}

//////////////////////

// it calls the sendTweet(Tweet) safely.
suspend fun program(): Either<Throwable, Unit> =
    sendTweet(Tweet)

// it calls sendTweet(Tweet) in a blocking way without making the function suspend.
suspend fun blockingProgram(): Either<Throwable, Unit> =
    runBlocking { sendTweet(Tweet) }

// it calls sendTweet(Tweet) in an async way using a CoroutineScope (inheriting the coroutine / scope from the parent)
suspend fun nonBlockingProgramAsync(): Deferred<Either<Throwable, Unit>> =
    coroutineScope {
        async {
            sendTweet(Tweet)
        }
    }

suspend fun main() {
    program()

    blockingProgram()

    // deferred coroutine
    val deferred = nonBlockingProgramAsync()
    println(deferred)
}
