import Errors.*
import arrow.core.Either
import arrow.core.continuations.either
import models.Band

// Suspend
// Flattening callbacks
// Either.catch {} allows us to catch any exceptions thrown by a computation and lift them into Either.

suspend fun loadBand(id: String): Band = throw RuntimeException("Boom!")

suspend fun program(id: String): Either<Throwable, Band> =
    Either.catch { loadBand(id) }

// We can then use Either#mapLeft to map those throwable to some strongly typed domain errors.

//////////////////////

sealed class Errors : Throwable() {
    object UserTokenError : Errors()
    object CreatePostError : Errors()
    object SendPostError : Errors()
}

object Tweet
typealias UserToken = String

data class Post(val tweet: Tweet)

// requestUserToken(), createPost() and sendPost() to catch their own errors and
// map them to the corresponding domain errors.
suspend fun requestUserToken(): Either<UserTokenError, UserToken> =
    Either.catch { throw RuntimeException("User token not found!") }
        .mapLeft { UserTokenError }

suspend fun createPost(token: String, tweet: Tweet): Either<CreatePostError, Post> =
    Either.catch { throw RuntimeException("Post could not be created!") }
        .mapLeft { CreatePostError }

suspend fun sendPost(post: Post): Either<SendPostError, Unit> =
    Either.catch { throw RuntimeException("Error on sending the post!") }
        .mapLeft { SendPostError }

suspend fun sendTweet(tweet: Tweet): Either<Throwable, Unit> =
    either {
        val userToken = requestUserToken().bind()
        val post = createPost(userToken, tweet).bind()
        sendPost(post).bind()
    }

suspend fun main() {
    val tweet = sendTweet(Tweet)
    println(tweet)
}
