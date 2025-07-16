@file:Suppress("Unused", "UnusedPrivateMember", "MagicNumber")

package language

import arrow.continuations.SuspendApp
import arrow.core.Nel
import arrow.core.raise.Raise
import arrow.core.raise.recover
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.parZipOrAccumulate
import arrow.fx.coroutines.resourceScope
import kotlinx.coroutines.delay

/** Kotlin's receivers, extension functions and alternatives to context receivers */
object Receivers {

  // extension function
  fun String.toNumberPlus(): Double = this.toDouble() + 1

  fun String.transformToNumber(block: String.() -> Double): Double = this.block()

  // context receiver
  context(Raise<Nel<Throwable>>, ResourceScope)
  suspend fun getBalanceCall(): Pair<String, Double> =
    parZipOrAccumulate(
      {
        // some heavy computation
        delay(500L)
        "David's balance is $744,654,986.00"
      },
      {
        // some heavy computation
        delay(500L)
        744654986.00
      },
    ) { text, amount ->
      text to amount
    }

  /**
   * At first, the initial alternative approach was to use delegation to merge the contexts from
   * `raise` and `resourceScope`, however, this doesn't work well when there are conflicting methods
   * in the contexts that have the same signature (in the case of `bind`). In any case, the code is
   * available below.
   */

  // first approach
  //  suspend fun <Ctx> Ctx.getBalance(): Pair<String, Double> where
  //  Ctx : Raise<Nel<Throwable>>,
  //  Ctx : ResourceScope = getBalanceCall()

  // Bridge contexts using a temporary object
  //  private fun createContext(raise: Raise<Nel<Throwable>>, scope: ResourceScope) =
  //    object : Raise<Nel<Throwable>> by raise, ResourceScope by scope {}

  // could also create an interface to make the object non-anonymous
  //  interface Context<Error> : Raise<Error>, ResourceScope
  //
  //  private fun createContextWithInterface(
  //    raise: Raise<Nel<Throwable>>,
  //    scope: ResourceScope,
  //  ): Context<Nel<Throwable>> =
  //    object : Context<Nel<Throwable>>, Raise<Nel<Throwable>> by raise, ResourceScope by scope {}

  // with generics
  //  private fun <Error> createContextWithInterfaceAndGenerics(
  //    raise: Raise<Error>,
  //    scope: ResourceScope,
  //  ): Context<Error> = object : Context<Error>, Raise<Error> by raise, ResourceScope by scope {}

  // second approach
  //  class ResourceContext<Error>(
  //    private val raise: Raise<Error>,
  //    private val resourceScope: ResourceScope,
  //  ) : Raise<Error> by raise, ResourceScope by resourceScope

  // with DSL and common resource life cycle (e.g. clients, db connections)
  //  suspend fun <A, Error> resourceContext(
  //    block: suspend ResourceContext<Nel<Error>>.() -> A,
  //    err: (e: Nel<Error>) -> A,
  //  ) {
  //    resourceScope {
  //      recover({
  //        with(ResourceContext<Nel<Error>>(this@recover, this@resourceScope)) { block() }
  //      }) { e ->
  //        err(e)
  //      }
  //    }
  //  }
  //  @JvmStatic
  //  fun main(args: Array<String>) = SuspendApp {
  // extension function
  //    val balance = "744654986.00"
  //    println(balance.toNumberPlus())

  // fn taking a lambda with a receiver
  //    val balance2 = balance.transformToNumber { this.toNumberPlus() }
  //    println(balance2)

  // From context receivers
  //    resourceScope {
  //      recover({
  // use the context
  //         val ctx = createContext(this@recover, this@resourceScope)
  //         val balance = ctx.getBalance()
  //         println(balance)
  //
  //         val ctx2 = createContextWithInterface(this@recover, this@resourceScope)
  //         val balance2 = ctx2.getBalance()
  //         println(balance2)

  // or use the class context
  //         val klassCtx = ResourceContext(this@recover, this@resourceScope)
  //         val klassBalance = klassCtx.getBalance()
  //         println(klassBalance)
  //      }) { errors ->
  //        errors.forEach { e -> println(e) }
  //      }
  //    }

  // DSL version
  //    resourceContext({
  //      val klassBalance = getBalance()
  //      println(klassBalance)
  //    }) { errors ->
  //      errors.forEach { e -> println(e) }
  //    }

  // cancel()
  //  }

  /**
   * Hence, after Kotlin 2.2.0 and Arrow 2.1.2, there is no option but to pass the contexts as
   * parameters, and build the contexts. The 'official' way to do it is to use context parameters,
   * or context receivers for previous versions of Kotlin. Otherwise, they must be passed as params.
   */
  suspend fun getBalance(
    raise: Raise<Nel<Throwable>>,
    resourceScope: ResourceScope,
  ): Pair<String, Double> = with(raise) { with(resourceScope) { getBalanceCall() } }

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    // extension function
    val balance = "744654986.00"
    println(balance.toNumberPlus())

    // fn taking a lambda with a receiver
    val balance2 = balance.transformToNumber { this.toNumberPlus() }
    println(balance2)

    resourceScope {
      recover({
        val (balance, balance2) = getBalance(this@recover, this@resourceScope)
        println(balance)
        println(balance2)
      }) { errors ->
        errors.forEach { e -> println(e) }
      }
    }
  }
}
