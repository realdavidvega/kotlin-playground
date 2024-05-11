@file:Suppress("Unused", "MagicNumber")

package language

import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextInt
import kotlinx.datetime.LocalDate

// Data classes and data objects

object Objects {
  sealed class Tweet {
    data class Normal(val content: String) : Tweet()

    data class Scheduled(val date: LocalDate, val tweet: Tweet) : Tweet()

    data class Combined(val tweets: List<Tweet>) : Tweet()

    data object FunnyTone : Tweet() // better than object, after 1.9

    data object DadJoke : Tweet()
  }

  object DiceRoller {
    fun roll(sides: Int): Int = Random.nextInt(1..sides)

    fun rollMultiple(n: Int, sides: Int): Int = (1..n).sumOf { roll(sides) }

    fun rollWithAdvantage(sides: Int): Int = max(roll(sides), roll(sides))
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
