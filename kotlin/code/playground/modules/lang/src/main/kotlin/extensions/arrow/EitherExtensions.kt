@file:OptIn(ExperimentalTime::class)

package extensions.arrow

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import java.time.OffsetDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Handful extensions for [arrow.core.Either]
 *
 * Some of those extensions include `Either<Throwable, A>.recoverCatching` or `getOrRaise`
 */

/** Contextual raise [E] on [Either.Left] */
context(Raise<E>, Either<L, R>)
@OptIn(ExperimentalContracts::class)
fun <E, L, R> getOrRaise(block: () -> E): R {
  contract { callsInPlace(block, AT_MOST_ONCE) }
  return getOrElse { raise(block()) }
}

/** Recover from [Either.catch] or other [Either.recoverCatching] */
@OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
inline fun <A> Either<Throwable, A>.recoverCatching(
  @BuilderInference recover: (Throwable) -> A
): Either<Throwable, A> {
  contract { callsInPlace(recover, InvocationKind.AT_MOST_ONCE) }
  return when (this) {
    is Either.Left -> Either.catch { recover(value) }
    is Either.Right -> this@recoverCatching
  }
}

object EitherExtensions {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // recoverCatching example
      val date = "2023-01-01T00:00:00"
      val instantOrNull =
        Either.catch {
            println("Let's try to parse as an instant: $date")
            Instant.parse(date)
          }
          .recoverCatching {
            println("Oops, let's try to parse as an offset date time: $date")
            OffsetDateTime.parse(date).toInstant().toKotlinInstant()
          }
          .recoverCatching {
            println("Oops, let's try to parse as a local date time: $date")
            // assuming UTC
            LocalDateTime.parse(date).toInstant(TimeZone.UTC)
          }
          .getOrNull()

      println("The final value of instant is: $instantOrNull")
    }
  }
}
