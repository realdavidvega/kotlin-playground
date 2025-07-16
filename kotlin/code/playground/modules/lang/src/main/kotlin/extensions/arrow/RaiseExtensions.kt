package extensions.arrow

import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.ensureNotNull
import arrow.core.raise.recover
import extensions.Kotlin.Quadruple
import extensions.Kotlin.Quintuple
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlinx.coroutines.runBlocking

/**
 * Handful extensions for [arrow.core.raise.Raise]
 *
 * Some of those extensions include `ensureNotNull` with more than one argument.
 */
@OptIn(ExperimentalContracts::class)
@RaiseDSL
inline fun <Error, A : Any, B : Any> Raise<Error>.ensureNotNull(
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
inline fun <Error, A : Any, B : Any, C : Any> Raise<Error>.ensureNotNull(
  value1: A?,
  value2: B?,
  value3: C?,
  raise: () -> Error,
): Triple<A, B, C> {
  contract {
    callsInPlace(raise, InvocationKind.AT_MOST_ONCE)
    returns() implies (value1 != null && value2 != null && value3 != null)
  }
  if (value1 == null || value2 == null || value3 == null) raise(raise())
  else return Triple(value1, value2, value3)
}

@Suppress("ComplexCondition")
@OptIn(ExperimentalContracts::class)
@RaiseDSL
inline fun <Error, A : Any, B : Any, C : Any, D : Any> Raise<Error>.ensureNotNull(
  value1: A?,
  value2: B?,
  value3: C?,
  value4: D?,
  raise: () -> Error,
): Quadruple<A, B, C, D> {
  contract {
    callsInPlace(raise, InvocationKind.AT_MOST_ONCE)
    returns() implies (value1 != null && value2 != null && value3 != null && value4 != null)
  }
  if (value1 == null || value2 == null || value3 == null || value4 == null) raise(raise())
  else return Quadruple(value1, value2, value3, value4)
}

@Suppress("LongParameterList", "ComplexCondition")
@OptIn(ExperimentalContracts::class)
@RaiseDSL
inline fun <Error, A : Any, B : Any, C : Any, D : Any, E : Any> Raise<Error>.ensureNotNull(
  value1: A?,
  value2: B?,
  value3: C?,
  value4: D?,
  value5: E?,
  raise: () -> Error,
): Quintuple<A, B, C, D, E> {
  contract {
    callsInPlace(raise, InvocationKind.AT_MOST_ONCE)
    returns() implies
      (value1 != null && value2 != null && value3 != null && value4 != null && value5 != null)
  }
  if (value1 == null || value2 == null || value3 == null || value4 == null || value5 == null)
    raise(raise())
  else return Quintuple(value1, value2, value3, value4, value5)
}

object RaisesToWrappers {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // ensureNotNull examples
      class User(
        val id: Long? = null,
        val name: String? = null,
        val age: Int? = null,
        val email: String? = null,
        val phone: String? = null,
      )

      class UserError(val message: String)

      // some dummy user
      val user = User()

      /** normal arrow */
      recover({ ensureNotNull(user.id) { UserError("Oops, id is null") } }) { e -> println(e) }

      /** [ensureNotNull] with two arguments, returns a pair if succeeds */
      recover({ ensureNotNull(user.id, user.name) { UserError("Oops, id and name are null") } }) { e
        ->
        println(e)
      }

      /** [ensureNotNull] with three arguments, returns a triple if succeeds */
      recover({
        ensureNotNull(user.id, user.name, user.age) { UserError("Oops, id, name and age are null") }
      }) { e ->
        println(e)
      }

      /** [ensureNotNull] with four arguments, returns a quadruple if succeeds */
      recover({
        ensureNotNull(user.id, user.name, user.age, user.email) {
          UserError("Oops, id, name, age and email are null")
        }
      }) { e ->
        println(e)
      }

      /** [ensureNotNull] with five arguments, returns a quintuple if succeeds */
      recover({
        ensureNotNull(user.id, user.name, user.age, user.email, user.phone) {
          UserError("Oops, id, name, age, email and phone are null")
        }
      }) { e ->
        println(e)
      }
    }
  }
}
