@file:Suppress("MagicNumber")

package state

import arrow.atomic.Atomic
import arrow.atomic.AtomicBoolean
import arrow.atomic.AtomicInt
import arrow.atomic.update
import arrow.atomic.value
import arrow.fx.coroutines.parMap
import kotlinx.coroutines.runBlocking

/**
 * Atomicity
 *
 * Multiplatform atomicity with `arrow-atomic`.
 *
 * It provides Multiplatform-ready atomic references. In particular, their getAndSet, getAndUpdate,
 * and compareAndSet operations are guaranteed to happen atomically; there's no possibility of two
 * computations performing these operations and getting an inconsistent state at the end.
 */
object Atomicity {

  /*
   * You should not use generic Atomic references with primitive types like Int or Boolean,
   * as they break in unexpected ways in Kotlin Native. Instead, use the provided AtomicInt,
   * AtomicBoolean, and so forth.
   */

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
          UserStatus(LoggedIn(AtomicBoolean(false))),
        )

      // accessing
      println(user.status.loggedIn.atomic.get())

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
          UserId("2"),
          UserName("John Boe"),
          UserAge(AtomicInt(40)),
          UserStatus(LoggedIn(AtomicBoolean(false))),
        )

      (0..<1000).parMap {
        otherUser.status.loggedIn.atomic.update { currentStatus -> !currentStatus }
      }
      println("Final login status: ${otherUser.status.loggedIn.atomic.value}") // false

      println("------------------------------")

      // Atomically sets the value to newValue and returns the old value, with memory effects
      println("Login status: ${user.status.loggedIn.atomic.getAndSet(true)}") // true

      val newUser =
        User(
          UserId("3"),
          UserName("Jimmy Joe"),
          UserAge(AtomicInt(20)),
          UserStatus(LoggedIn(AtomicBoolean(false))),
        )

      // In the JVM it's an alias for `java.util.concurrent.atomic.AtomicReference<V>`
      val atomicUser: Atomic<User> = Atomic(newUser)

      // Atomically updates the current value with the results of applying the given function,
      // returning the updated value. The function should be side-effect-free, since it may be
      // re-applied when attempted updates fail due to contention among threads.
      atomicUser.updateAndGet { userWithBirthday ->
        userWithBirthday.age.atomic.updateAndGet { currentAge -> currentAge + 1 }
        userWithBirthday
      }
      println("New age: ${atomicUser.value.age.atomic.value}")

      // Atomically sets the value to newValue if the current value == expectedValue,
      // with memory effects
      atomicUser.update { userWithBirthday ->
        userWithBirthday.age.atomic.compareAndSet(21, 22)
        userWithBirthday
      }
      println("New age only in 2022: ${atomicUser.value.age.atomic.value}")
    }
  }
}
