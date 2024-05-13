@file:Suppress("MagicNumber")

package state

import arrow.atomic.AtomicBoolean
import arrow.atomic.AtomicInt
import arrow.atomic.update
import arrow.atomic.value
import arrow.fx.coroutines.parMap
import kotlinx.coroutines.runBlocking

// 4. Multiplatform atomicity with Arrow Atomic

object Atomicity {
  data class User(val id: UserId, val name: UserName, var age: UserAge, val status: UserStatus)

  data class UserStatus(val loggedIn: LoggedIn)

  @JvmInline value class LoggedIn(val atomic: AtomicBoolean)

  @JvmInline value class UserId(val value: String)

  @JvmInline value class UserName(val value: String)

  @JvmInline value class UserAge(val atomic: AtomicInt)

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val user =
        User(
          UserId("1"),
          UserName("John Doe"),
          UserAge(AtomicInt(30)),
          UserStatus(LoggedIn(AtomicBoolean(false)))
        )

      // extension value access
      println("Initial age: ${user.age.atomic.value}")
      println("Initial login status: ${user.status.loggedIn.atomic.value}")

      // Update age atomically
      user.age.atomic.update { currentAge -> currentAge + 1 }

      // Update login status atomically
      user.status.loggedIn.atomic.update { currentStatus -> !currentStatus }

      println("Updated age: ${user.age.atomic.value}")
      println("Updated login status: ${user.status.loggedIn.atomic.value}")

      println("------------------------------")

      // Parallel updates
      val otherUser =
        User(
          UserId("1"),
          UserName("John Doe"),
          UserAge(AtomicInt(30)),
          UserStatus(LoggedIn(AtomicBoolean(false)))
        )

      (0 ..< 1000).parMap {
        otherUser.status.loggedIn.atomic.update { currentStatus -> !currentStatus }
      }
      println("Final login status: ${otherUser.status.loggedIn.atomic.value}") // false
    }
  }
}
