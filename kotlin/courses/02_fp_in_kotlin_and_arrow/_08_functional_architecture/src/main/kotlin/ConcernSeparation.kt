import Errors.*
import arrow.core.Either
import arrow.core.continuations.either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors

// Functional Architecture
// Concern separation
// We have learned how it allows us to separate our pure logics (what the program does) from how it is consumed.

// A program that goes pure edge to edge can be understood as a function from input to output.
// It is easier to reason about and to test.

// We have learned how we can flag effects as suspend to make them compile time tracked and to make them require a
// prepared execution context (Coroutine). The compiler ensures we can't call our effects from anywhere.

//////////////////////

sealed class Errors {
    object UserTokenError : Errors()
    object CreatePostError : Errors()
    object SendPostError : Errors()
}

data class Tweet(val content: String)
typealias UserToken = String

data class Post(val tweet: Tweet)
data class PostedTweet(val post: Post, val date: Date)

fun requestUserToken(): Either<UserTokenError, UserToken> =
    Either.catch { "adok12093sldkko921389" }
        .mapLeft { UserTokenError }

fun createPost(token: String, tweet: Tweet): Either<CreatePostError, Post> =
    Either.catch { Post(tweet) }
        .mapLeft { CreatePostError }

fun sendPost(post: Post): Either<SendPostError, PostedTweet> =
    Either.catch { PostedTweet(post, Date(1620029763)) }
        .mapLeft { SendPostError }

fun sendTweet(tweet: Tweet): Either<Errors, PostedTweet> =
    either.eager {
        val userToken = requestUserToken().bind()
        val post = createPost(userToken, tweet).bind()
        sendPost(post).bind()
    }

fun program(callback: (Either<Errors, PostedTweet>) -> Unit) {
    val executor = Executors.newSingleThreadExecutor()
    // blocking
    executor.submit {
        val postedTweet = sendTweet(Tweet("Hello friends!"))
        callback(postedTweet)
    }
}

// Refactored the provided program to track all side effects as suspend. I.e: requestUserToken, createPost, sendPost.
// Refactored sendTweet, so it can call the newly suspended effects. Remember about either suspend computation block.
// Refactored program to provide a non-blocking runtime to consume the suspended program. To do that, get a
// coroutine dispatcher from the provided executor. I.e: ExecutorService#asCoroutineDispatcher().
// Use it to create a CoroutineScope and async the sendTweet function with it.
// Notify the result via the callback. (This is only to make sure the test can pass for both programming styles).
// Don't forget to await the job in the end to avoid the test to finish before the job completes.

suspend fun requestUserTokenSuspend(): Either<UserTokenError, UserToken> =
    Either.catch { "adok12093sldkko921389" }
        .mapLeft { UserTokenError }

suspend fun createPostSuspend(token: String, tweet: Tweet): Either<CreatePostError, Post> =
    Either.catch { Post(tweet) }
        .mapLeft { CreatePostError }

suspend fun sendPostSuspend(post: Post): Either<SendPostError, PostedTweet> =
    Either.catch { PostedTweet(post, Date(1620029763)) }
        .mapLeft { SendPostError }

suspend fun sendTweetSuspend(tweet: Tweet): Either<Errors, PostedTweet> =
    either {
        val userToken = requestUserTokenSuspend().bind()
        val post = createPostSuspend(userToken, tweet).bind()
        sendPostSuspend(post).bind()
    }

suspend fun programSuspend(callback: (Either<Errors, PostedTweet>) -> Unit) {
    val executor = Executors.newSingleThreadExecutor()
    val coroutineScope = CoroutineScope(executor.asCoroutineDispatcher())

    val deferred = coroutineScope.async {
        val postedTweet = sendTweetSuspend(Tweet("Hello friends!"))
        callback(postedTweet)
    }
    deferred.await()
}

suspend fun main() {
    programSuspend { println(it) }
    // Either.Right(PostedTweet(post=Post(tweet=Tweet(content=Hello friends!)), date=Mon Jan 19 19:00:29 CET 1970))
}
