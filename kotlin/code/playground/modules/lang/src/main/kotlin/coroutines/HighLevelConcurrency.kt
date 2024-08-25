@file:Suppress("Unused", "MagicNumber", "UnusedPrivateProperty")

package coroutines

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.toNonEmptyListOrNull
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapOrAccumulate
import arrow.fx.coroutines.parZip
import arrow.fx.coroutines.raceN
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * (4) High level concurrency
 *
 * With Arrow Fx Coroutines using `arrow.fx.coroutines`.
 */
object HighLevelConcurrency {
  @JvmInline value class UserId(val value: Long)

  @JvmInline value class UserName(val value: String)

  @JvmInline value class Email(val value: String)

  data class User(val id: UserId, val username: UserName, val email: Email)

  val USER_DATABASE =
    mapOf(
      UserId(1) to User(UserId(1), UserName("shadow"), Email("shadow@mail.com")),
      UserId(2) to User(UserId(2), UserName("cloud"), Email("cloud@mail.com")),
      UserId(3) to User(UserId(3), UserName("outbreak"), Email("outbreak@mail.com")),
      UserId(4) to User(UserId(4), UserName("rhino"), Email("rhino@mail.com")),
    )

  @JvmInline value class TweetId(val value: Long)

  @JvmInline value class Content(val value: String)

  data class Tweet(val id: TweetId, val userId: UserId, val content: Content)

  val TWEET_DATABASE =
    mapOf(
      TweetId(1) to Tweet(TweetId(1), UserId(1), Content("Kotlin is awesome!")),
      TweetId(2) to Tweet(TweetId(2), UserId(1), Content("Morning Kotliners!")),
      TweetId(3) to Tweet(TweetId(3), UserId(2), Content("Today is sunny in BCN")),
    )

  sealed interface Error {
    data class Generic(val detail: String) : Error {
      override fun toString(): String = "unexpected error happened, detail: $detail"
    }

    sealed interface User : Error {
      data class NotFound(val id: UserId) : User {
        override fun toString(): String = "user with id '${id.value}' was not found"
      }

      data object EmptyList : User {
        override fun toString(): String = "the list cannot be empty"
      }
    }

    sealed interface Tweet : Error {
      data class NotFound(val id: TweetId) : Tweet {
        override fun toString(): String = "tweet with id '${id.value}' was not found"
      }
    }
  }

  interface Users {
    context(Raise<Error>)
    suspend fun getUserName(userId: UserId): UserName

    context(Raise<Error>)
    suspend fun getAllUsers(): NonEmptyList<User>

    companion object {
      operator fun invoke(): Users =
        object : Users {
          context(Raise<Error>)
          override suspend fun getUserName(userId: UserId): UserName =
            withContext(Dispatchers.IO) {
              USER_DATABASE[userId]?.username ?: raise(Error.User.NotFound(userId))
            }

          context(Raise<Error>)
          override suspend fun getAllUsers(): NonEmptyList<User> =
            withContext(Dispatchers.IO) {
              catch({
                // non-empty list data structure, or raise error
                USER_DATABASE.values.toNonEmptyListOrNull() ?: raise(Error.User.EmptyList)
              }) { t: Throwable ->
                raise(Error.Generic(t.message ?: "fatal error"))
              }
            }
        }
    }
  }

  interface Tweets {
    context(Raise<Error>)
    suspend fun getTweets(userId: UserId): List<Tweet>

    companion object {
      operator fun invoke(): Tweets =
        object : Tweets {
          context(Raise<Error>)
          override suspend fun getTweets(userId: UserId): List<Tweet> =
            withContext(Dispatchers.IO) {
              catch({ TWEET_DATABASE.values.filter { tweet -> tweet.userId == userId } }) {
                t: Throwable ->
                raise(Error.Generic(t.message ?: "fatal error"))
              }
            }
        }
    }
  }

  interface TweetService {
    context(Raise<Error>)
    suspend fun getTweetsForUserName(userId: UserId): Pair<UserName, List<Tweet>>

    context(Raise<Error>)
    suspend fun getTweetsForIds(userIds: List<UserId>): List<Pair<UserId, List<Tweet>>>

    suspend fun getTweetsForIds2(
      userIds: NonEmptyList<UserId>
    ): Either<NonEmptyList<Error>, List<Pair<UserId, List<Tweet>>>>

    context(Raise<Error>)
    suspend fun getFastestUser(user1: UserId, user2: UserId): UserName

    companion object {
      operator fun invoke(users: Users, tweets: Tweets): TweetService =
        object : TweetService {
          context(Raise<Error>)
          override suspend fun getTweetsForUserName(userId: UserId): Pair<UserName, List<Tweet>> =
            // independently, in parallel
            parZip(
              { users.getUserName(userId) },
              { tweets.getTweets(userId) },
              { userName, tweets -> Pair(userName, tweets) },
            )

          context(Raise<Error>)
          override suspend fun getTweetsForIds(
            userIds: List<UserId>
          ): List<Pair<UserId, List<Tweet>>> =
            users
              .getAllUsers()
              .filter { user -> user.id in userIds }
              .parMap { user ->
                // wrapper needed
                val tweetList = either { tweets.getTweets(user.id) }.getOrElse { raise(it) }
                Pair(user.id, tweetList)
              }

          override suspend fun getTweetsForIds2(
            userIds: NonEmptyList<UserId>
          ): Either<NonEmptyList<Error>, List<Pair<UserId, List<Tweet>>>> =
            either { users.getAllUsers() }
              .map { userList ->
                userIds.parMapOrAccumulate { id ->
                  val user =
                    userList.find { user -> user.id == id } ?: raise(Error.User.NotFound(id))
                  val tweetList = tweets.getTweets(user.id)
                  Pair(user.id, tweetList)
                }
              }
              .getOrElse { error -> nonEmptyListOf(error).left() }

          context(Raise<Error>)
          override suspend fun getFastestUser(user1: UserId, user2: UserId): UserName =
            ensure(user1 != user2) { Error.Generic("user ids cannot be the same") }
              .let {
                raceN({ users.getUserName(user1) }, { users.getUserName(user2) })
                  .fold({ userOne -> userOne }, { userTwo -> userTwo })
              }
        }
    }
  }

  private suspend fun logCancellation(): Unit =
    try {
      println("Sleeping for 500 ms...")
      delay(500)
    } catch (e: CancellationException) {
      println("Sleep was cancelled early!")
      throw e
    }

  private suspend fun Raise<String>.failOnEven(i: Int): Unit {
    ensure(i % 2 != 0) {
      delay(100)
      "Not even error!"
    }
    logCancellation()
  }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val users = Users()
      val tweets = Tweets()
      val service = TweetService(users, tweets)

      // get tweets by username
      either { service.getTweetsForUserName(UserId(1)) }
        .map { (userName, tweets) ->
          println("The user '${userName.value}' has ${tweets.size} tweets")
        }
        .getOrElse { error -> println(error) }

      println("------------------------------")

      // get tweets for ids
      either { service.getTweetsForIds(listOf(UserId(1), UserId(2))) }
        .map { userList ->
          userList.forEach { (id, tweets) ->
            println("The user with id '${id.value}' has ${tweets.size} tweets")
          }
        }
        .getOrElse { error -> println(error) }

      println("------------------------------")

      // get tweets for ids, accumulating errors
      service
        .getTweetsForIds2(nonEmptyListOf(UserId(5), UserId(6)))
        .map { userList ->
          userList.forEach { (id, tweets) ->
            println("The user with id '${id.value}' has ${tweets.size} tweets")
          }
        }
        .getOrElse { errors -> errors.forEach { error -> println(error) } }

      println("------------------------------")

      // racing
      either { service.getFastestUser(UserId(1), UserId(1)) }
        .map { user -> println("The fastest user to retrieve was '${user.value}'") }
        .getOrElse { error -> println(error) }

      println("------------------------------")

      // typed errors
      parZip(
        { either<String, Unit> { logCancellation() } },
        {
          either<String, Unit> {
            delay(100)
            raise("Error")
          }
        },
        { either<String, Unit> { logCancellation() } },
      ) { a, b, c ->
        Triple(a, b, c)
      }

      println("------------------------------")

      // parZip cancellation on raise
      either {
          parZip(
            { logCancellation() },
            {
              delay(100)
              raise("Error!")
            }, // early cancellation
            { logCancellation() },
          ) { a, b, c ->
            Triple(a, b, c)
          } // unreachable
        }
        .getOrElse { error -> println(error) }

      println("------------------------------")

      // parMap cancellation on raise
      either { listOf(1, 2, 3, 4).parMap { failOnEven(it) } }.getOrElse { error -> println(error) }

      println("------------------------------")

      // other accumulation example
      listOf(1, 2, 3, 4)
        .parMapOrAccumulate { failOnEven(it) }
        .getOrElse { errors -> errors.forEach { error -> println(error) } }
    }
  }
}
