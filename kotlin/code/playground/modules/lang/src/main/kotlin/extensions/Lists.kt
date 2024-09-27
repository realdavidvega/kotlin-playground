package extensions

import extensions.Lists.second
import extensions.Lists.secondOrNull
import extensions.Lists.third
import extensions.Lists.thirdOrNull

/**
 * Handful extensions for `kotlin.collections.List`.
 *
 * Some of those extensions include accessors for the second, third, fourth... elements of a list
 */
object Lists {
  private const val SECOND_INDEX = 1
  private const val THIRD_INDEX = 2
  private const val FOURTH_INDEX = 3
  private const val FIFTH_INDEX = 4
  private const val SIXTH_INDEX = 5
  private const val SEVENTH_INDEX = 6
  private const val EIGHTH_INDEX = 7
  private const val NINTH_INDEX = 8
  private const val TENTH_INDEX = 9

  /**
   * Returns the second element of the list.
   *
   * @param T the type of elements in this list.
   * @return the second element of this list.
   * @throws NoSuchElementException if the list contains fewer than two elements.
   */
  fun <T> List<T>.second(): T =
    if (size <= SECOND_INDEX) throw NoSuchElementException("List contains fewer than two elements.")
    else this[SECOND_INDEX]

  /**
   * Returns the second element of the list or null if the list has fewer than two elements.
   *
   * @param T the type of elements in this list.
   * @return the second element of this list or null if the list has fewer than two elements.
   */
  fun <T> List<T>.secondOrNull(): T? =
    if (size <= SECOND_INDEX) null else this[SECOND_INDEX]

  /**
   * Returns the third element of the list.
   *
   * @param T the type of elements in this list.
   * @return the third element of this list.
   * @throws NoSuchElementException if the list contains fewer than three elements.
   */
  fun <T> List<T>.third(): T =
    if (size <= THIRD_INDEX) throw NoSuchElementException("List contains fewer than three elements.")
    else this[THIRD_INDEX]

  /**
   * Returns the third element of the list or null if the list has fewer than three elements.
   *
   * @param T the type of elements in this list.
   * @return the third element of this list or null if the list has fewer than three elements.
   */
  fun <T> List<T>.thirdOrNull(): T? =
    if (size <= THIRD_INDEX) null else this[THIRD_INDEX]

  /**
   * Returns the fourth element of the list.
   *
   * @param T the type of elements in this list.
   * @return the fourth element of this list.
   * @throws NoSuchElementException if the list contains fewer than four elements.
   */
  fun <T> List<T>.fourth(): T =
    if (size <= FOURTH_INDEX) throw NoSuchElementException("List contains fewer than four elements.")
    else this[FOURTH_INDEX]

  /**
   * Returns the fourth element of the list or null if the list has fewer than four elements.
   *
   * @param T the type of elements in this list.
   * @return the fourth element of this list or null if the list has fewer than four elements.
   */
  fun <T> List<T>.fourthOrNull(): T? =
    if (size <= FOURTH_INDEX) null else this[FOURTH_INDEX]

  /**
   * Returns the fifth element of the list.
   *
   * @param T the type of elements in this list.
   * @return the fifth element of this list.
   * @throws NoSuchElementException if the list contains fewer than five elements.
   */
  fun <T> List<T>.fifth(): T =
    if (size <= FIFTH_INDEX) throw NoSuchElementException("List contains fewer than five elements.")
    else this[FIFTH_INDEX]

  /**
   * Returns the fifth element of the list or null if the list has fewer than five elements.
   *
   * @param T the type of elements in this list.
   * @return the fifth element of this list or null if the list has fewer than five elements.
   */
  fun <T> List<T>.fifthOrNull(): T? =
    if (size <= FIFTH_INDEX) null else this[FIFTH_INDEX]

  /**
   * Returns the sixth element of the list.
   *
   * @param T the type of elements in this list.
   * @return the sixth element of this list.
   * @throws NoSuchElementException if the list contains fewer than six elements.
   */
  fun <T> List<T>.sixth(): T =
    if (size <= SIXTH_INDEX) throw NoSuchElementException("List contains fewer than six elements.")
    else this[SIXTH_INDEX]

  /**
   * Returns the sixth element of the list or null if the list has fewer than six elements.
   *
   * @param T the type of elements in this list.
   * @return the sixth element of this list or null if the list has fewer than six elements.
   */
  fun <T> List<T>.sixthOrNull(): T? =
    if (size <= SIXTH_INDEX) null else this[SIXTH_INDEX]

  /**
   * Returns the seventh element of the list.
   *
   * @param T the type of elements in this list.
   * @return the seventh element of this list.
   * @throws NoSuchElementException if the list contains fewer than seven elements.
   */
  fun <T> List<T>.seventh(): T =
    if (size <= SEVENTH_INDEX) throw NoSuchElementException("List contains fewer than seven elements.")
    else this[SEVENTH_INDEX]

  /**
   * Returns the seventh element of the list or null if the list has fewer than seven elements.
   *
   * @param T the type of elements in this list.
   * @return the seventh element of this list or null if the list has fewer than seven elements.
   */
  fun <T> List<T>.seventhOrNull(): T? =
    if (size <= SEVENTH_INDEX) null else this[SEVENTH_INDEX]

  /**
   * Returns the eighth element of the list.
   *
   * @param T the type of elements in this list.
   * @return the eighth element of this list.
   * @throws NoSuchElementException if the list contains fewer than eight elements.
   */
  fun <T> List<T>.eighth(): T =
    if (size <= EIGHTH_INDEX) throw NoSuchElementException("List contains fewer than eight elements.")
    else this[EIGHTH_INDEX]

  /**
   * Returns the eighth element of the list or null if the list has fewer than eight elements.
   *
   * @param T the type of elements in this list.
   * @return the eighth element of this list or null if the list has fewer than eight elements.
   */
  fun <T> List<T>.eighthOrNull(): T? =
    if (size <= EIGHTH_INDEX) null else this[EIGHTH_INDEX]

  /**
   * Returns the ninth element of the list.
   *
   * @param T the type of elements in this list.
   * @return the ninth element of this list.
   * @throws NoSuchElementException if the list contains fewer than nine elements.
   */
  fun <T> List<T>.ninth(): T =
    if (size <= NINTH_INDEX) throw NoSuchElementException("List contains fewer than nine elements.")
    else this[NINTH_INDEX]

  /**
   * Returns the ninth element of the list or null if the list has fewer than nine elements.
   *
   * @param T the type of elements in this list.
   * @return the ninth element of this list or null if the list has fewer than nine elements.
   */
  fun <T> List<T>.ninthOrNull(): T? =
    if (size <= NINTH_INDEX) null else this[NINTH_INDEX]

  /**
   * Returns the tenth element of the list.
   *
   * @param T the type of elements in this list.
   * @return the tenth element of this list.
   * @throws NoSuchElementException if the list contains fewer than ten elements.
   */
  fun <T> List<T>.tenth(): T =
    if (size <= TENTH_INDEX) throw NoSuchElementException("List contains fewer than ten elements.")
    else this[TENTH_INDEX]

  /**
   * Returns the tenth element of the list or null if the list has fewer than ten elements.
   *
   * @param T the type of elements in this list.
   * @return the tenth element of this list or null if the list has fewer than ten elements.
   */
  fun <T> List<T>.tenthOrNull(): T? =
    if (size <= TENTH_INDEX) null else this[TENTH_INDEX]
}

private object ListTests {
  @JvmStatic
  fun main(args: Array<String>) {
    val numbers = listOf(1, 2)

    val second = numbers.second()
    println(second)

    val secondOrNull = numbers.secondOrNull()
    println(secondOrNull)

    try {
      numbers.third()
    } catch (e: NoSuchElementException) {
      println(e.message)
    }

    val thirdOrNull = numbers.thirdOrNull()
    println(thirdOrNull)
  }
}
