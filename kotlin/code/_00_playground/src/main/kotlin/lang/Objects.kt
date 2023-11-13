@file:Suppress("Unused", "MagicNumber")

package lang

import kotlinx.datetime.LocalDate

object Objects {
  sealed class Tweet {
    data class Normal(val content: String) : Tweet()

    data class Scheduled(val date: LocalDate, val tweet: Tweet) : Tweet()

    data class Combined(val tweets: List<Tweet>) : Tweet()

    data object FunnyTone : Tweet() // better than object, after 1.9

    data object DadJoke : Tweet()
  }

  fun main() {
    val tweet = Tweet.Normal("Love Kotlin!")
    println(tweet)

    val date = LocalDate(2023, 1, 1) // kotlin dates
    val nightTweet = Tweet.Scheduled(date, Tweet.Normal("Love Kotlin in the night!"))
    println(nightTweet)

    val showTweet = Tweet.Combined(listOf(Tweet.FunnyTone, Tweet.DadJoke))
    val scheduledShow = Tweet.Scheduled(date, showTweet)
    println(scheduledShow)
  }
}
