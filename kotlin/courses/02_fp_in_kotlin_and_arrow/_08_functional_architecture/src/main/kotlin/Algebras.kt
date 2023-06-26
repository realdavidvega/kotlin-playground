import Errors.*
import arrow.core.Either
import arrow.core.continuations.either
import java.util.*

// Functional Architecture
// Algebras
// Are comprised by a set of objects and the operations to work with those. The operations can be combined to
// create new objects that also belong to the set.

// In the context of Functional Programming, our immutable data (that many times is wrapped in a functional data type)
// would be the objects, and the pure functions would be the operations that work with those.
// Pure functions take some input data, transform it and operate over it to create some output data.

// We've learned that a couple of potential ways to encode algebras is via interfaces or sum types.

//////////////////////

// An algebra with an interface that represents a domain of operations we can perform over our network service.
// I.e: requestUserToken, createPost, sendPost.
// Create an implementation for the algebra and move the three suspended effects to it.
// Use that algebra from the sendTweet function instead of calling the effects directly.
// The test should keep passing.
// Note how this is no different from standard dependency inversion. We depend on abstractions when we need to have
// swappable implementation details (with tests in mind). We just look at it from the perspective of
// Functional Programming, since our effectual functions are flagged as suspend to enforce them being
// treated as controlled effects.

// Example 1
sealed class TweetNetworkOps<out A> {
    object RequestUserToken : TweetNetworkOps<Either<UserTokenError, UserToken>>()
    data class CreatePost(val token: String, val tweet: Tweet) : TweetNetworkOps<Either<CreatePostError, Post>>()
    data class SendPost(val post: Post) : TweetNetworkOps<Either<SendPostError, PostedTweet>>()
    data class SendTweet(val tweet: Tweet) : TweetNetworkOps<Either<Errors, PostedTweet>>()
}

suspend fun <A> interpreter(ops: TweetNetworkOps<A>): A = when(ops) {
    is TweetNetworkOps.RequestUserToken -> requestUserTokenSuspend()
    is TweetNetworkOps.CreatePost -> createPostSuspend(ops.token, ops.tweet)
    is TweetNetworkOps.SendPost -> sendPostSuspend(ops.post)
    is TweetNetworkOps.SendTweet -> sendTweetSuspend(ops.tweet)
} as A

// Example 2
interface TwitterAlgebra {
    suspend fun requestUserToken(): Either<UserTokenError, UserToken>
    suspend fun createPost(token: String, tweet: Tweet): Either<CreatePostError, Post>
    suspend fun sendPost(post: Post): Either<SendPostError, PostedTweet>
}

class TwitterService : TwitterAlgebra {
    override suspend fun requestUserToken(): Either<UserTokenError, UserToken> =
        Either.catch { "adok12093sldkko921389" }
            .mapLeft { UserTokenError }

    override suspend fun createPost(token: String, tweet: Tweet): Either<CreatePostError, Post> =
        Either.catch { Post(tweet) }
            .mapLeft { CreatePostError }

    override suspend fun sendPost(post: Post): Either<SendPostError, PostedTweet> =
        Either.catch { PostedTweet(post, Date(1620029763)) }
            .mapLeft { SendPostError }
}

suspend fun sendTweetWithService(tweet: Tweet): Either<Errors, PostedTweet> {
    val service  = TwitterService()
    return either {
        val userToken = service.requestUserToken().bind()
        val post = service.createPost(userToken, tweet).bind()
        service.sendPost(post).bind()
    }
}

suspend fun main() {
    // example 1
    val userToken = interpreter(TweetNetworkOps.RequestUserToken)
    println(userToken)

    // example 2
    val tweet = sendTweetWithService(Tweet("Hello"))
    println(tweet)
}
