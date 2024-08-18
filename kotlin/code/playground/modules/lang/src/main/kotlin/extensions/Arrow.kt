package extensions

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import java.time.OffsetDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinInstant

/** Handful extensions for [arrow.core] */
object Arrow {
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

  @OptIn(ExperimentalContracts::class)
  @RaiseDSL
  public inline fun <Error, A : Any, B : Any> Raise<Error>.ensureNotNull(
    value1: A?,
    value2: B?,
    raise: () -> Error,
  ): Pair<A, B> {
    contract {
      callsInPlace(raise, InvocationKind.AT_MOST_ONCE)
      returns() implies (value1 != null && value2 != null)
    }
    if (value1 == null || value2 == null) raise(raise()) else return value1 to value2
  }

  @OptIn(ExperimentalContracts::class)
  @RaiseDSL
  public inline fun <Error, A : Any, B : Any, C : Any> Raise<Error>.ensureNotNull(
    value1: A?,
    value2: B?,
    value3: C?,
    raise: () -> Error,
  ): Triple<A, B, C> {
    contract {
      callsInPlace(raise, InvocationKind.AT_MOST_ONCE)
      returns() implies (value1 != null && value2 != null)
    }
    if (value1 == null || value2 == null || value3 == null) raise(raise())
    else return Triple(value1, value2, value3)
  }

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
