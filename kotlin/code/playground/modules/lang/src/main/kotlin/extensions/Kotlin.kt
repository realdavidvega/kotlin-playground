package extensions

import java.io.Serializable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Handful extensions for `kotlin`.
 *
 * Some of those extensions include [Quadruple] and [Quintuple] types.
 */
object Kotlin {
  /**
   * Represents a quartet of values
   *
   * There is no meaning attached to values in this class, it can be used for any purpose. Quadruple
   * exhibits value semantics.
   *
   * @param A type of the first value.
   * @param B type of the second value.
   * @param C type of the third value.
   * @param D type of the fourth value.
   * @property first First value.
   * @property second Second value.
   * @property third Third value.
   * @property fourth Fourth value.
   */
  data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
  ) : Serializable {

    /**
     * Returns string representation of the [Triple] including its [first], [second], [third] and
     * [fourth] values.
     */
    override fun toString(): String = "($first, $second, $third, $fourth)"
  }

  /**
   * Represents a quintet of values
   *
   * There is no meaning attached to values in this class, it can be used for any purpose. Quadruple
   * exhibits value semantics.
   *
   * @param A type of the first value.
   * @param B type of the second value.
   * @param C type of the third value.
   * @param D type of the fourth value.
   * @param E type of the fifth value.
   * @property first First value.
   * @property second Second value.
   * @property third Third value.
   * @property fourth Fourth value.
   * @property fifth Fifth value.
   */
  data class Quintuple<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
  ) : Serializable {

    /**
     * Returns string representation of the [Triple] including its [first], [second], [third],
     * [fourth] and [fifth] values.
     */
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
  }

  /** Converts this quadruple into a list. */
  fun <T> Quadruple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)

  /** Converts this quadruple into a list. */
  fun <T> Quintuple<T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth)

  /** Holds a boolean value and allows local modification. */
  class BooleanHolder(var value: Boolean) {
    fun setTrue() {
      value = true
    }

    fun setFalse() {
      value = false
    }

    fun toggle() {
      value = !value
    }
  }

  /** Builds a boolean value from a lambda, allowing local modification, and returns the value. */
  inline fun buildBoolean(
    initialValue: Boolean = false,
    builderAction: BooleanHolder.() -> Unit,
  ): Boolean {
    val holder = BooleanHolder(initialValue)
    builderAction(holder)
    return holder.value
  }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val quadruple = Quadruple(1, 2, 3, 4)
      println(quadruple)

      val quintuple = Quintuple(1, 2, 3, 4, 5)
      println(quintuple)

      // example program that uses buildBoolean while loop is true and then exits it when false
      var workingHours = 0
      while (true) {
        val condition = buildBoolean {
          if (workingHours == 9) {
            println("I've reached my working hours limit today, wrapping up!")
            delay(500L)
            setTrue()
          }
        }
        if (condition) {
          println("I'm out, goodbye!")
          break
        }

        println("I'm working... don't disturb me! I've worked $workingHours hours so far")
        delay(1000L)
        workingHours++
      }
    }
  }
}
