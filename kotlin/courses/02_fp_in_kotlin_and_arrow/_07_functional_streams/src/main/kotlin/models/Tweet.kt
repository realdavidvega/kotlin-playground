package models

import arrow.core.Either
import arrow.core.continuations.either

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
suspend fun requestUserToken(): Either<Errors.UserTokenError, UserToken> =
    Either.catch { throw RuntimeException("User token not found!") }
        .mapLeft { Errors.UserTokenError }

suspend fun createPost(token: String, tweet: Tweet): Either<Errors.CreatePostError, Post> =
    Either.catch { throw RuntimeException("Post could not be created!") }
        .mapLeft { Errors.CreatePostError }

suspend fun sendPost(post: Post): Either<Errors.SendPostError, Unit> =
    Either.catch { throw RuntimeException("Error on sending the post!") }
        .mapLeft { Errors.SendPostError }

suspend fun sendTweet(tweet: Tweet): Either<Throwable, Unit> =
    either {
        val userToken = requestUserToken().bind()
        val post = createPost(userToken, tweet).bind()
        sendPost(post).bind()
    }