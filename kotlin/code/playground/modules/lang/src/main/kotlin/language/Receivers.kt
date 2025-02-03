@file:Suppress("Unused", "UnusedPrivateMember", "MagicNumber")

package language

import arrow.continuations.SuspendApp
import arrow.core.Nel
import arrow.core.raise.Raise
import arrow.core.raise.recover
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.parZipOrAccumulate
import arrow.fx.coroutines.resourceScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

/** Kotlin's receivers, extension functions and alternatives to context receivers  */
object Receivers {

  // extension function
  fun String.toNumberPlus(): Double = this.toDouble() + 1

  fun String.transformToNumber(
    block: String.() -> Double
  ): Double = this.block()

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
      }
    ) { text, amount ->
      text to amount
    }

  // first approach
  suspend fun <Ctx> Ctx.getBalance(): Pair<String, Double>
    where Ctx : Raise<Nel<Throwable>>,
          Ctx : ResourceScope = getBalanceCall()

  /** Bridge contexts using a temporary object */
  private fun createContext(
    raise: Raise<Nel<Throwable>>,
    scope: ResourceScope
  ) = object : Raise<Nel<Throwable>> by raise, ResourceScope by scope {}

  // could also create an interface to make the object non-anonymous
  interface Context<Error> : Raise<Error>, ResourceScope

  private fun createContextWithInterface(
    raise: Raise<Nel<Throwable>>,
    scope: ResourceScope
  ): Context<Nel<Throwable>> = object :
    Context<Nel<Throwable>>, Raise<Nel<Throwable>> by raise, ResourceScope by scope {}

  // with generics
  private fun <Error> createContextWithInterfaceAndGenerics(
    raise: Raise<Error>,
    scope: ResourceScope
  ): Context<Error> = object : Context<Error>, Raise<Error> by raise, ResourceScope by scope {}

  // second approach
  class ClassContext<Error>(
    private val raise: Raise<Error>,
    private val resourceScope: ResourceScope
  ) : Raise<Error> by raise, ResourceScope by resourceScope

  suspend fun ClassContext<Nel<Throwable>>.getBalance(): Pair<String, Double> = getBalanceCall()

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    // extension function
    val balance = "744654986.00"
    println(balance.toNumberPlus())

    // fn taking a lambda with a receiver
    val balance2 = balance.transformToNumber {
      this.toNumberPlus()
    }
    println(balance2)

    // from context receivers
    resourceScope {
      recover(
        {
          // use the context
          val ctx = createContext(this@recover, this@resourceScope)
          val balance = ctx.getBalance()
          println(balance)

          val ctx2 = createContextWithInterface(this@recover, this@resourceScope)
          val balance2 = ctx2.getBalance()
          println(balance2)

          // or use the class context
          val klassCtx = ClassContext(this@recover, this@resourceScope)
          val klassBalance = klassCtx.getBalance()
          println(klassBalance)
        }
      ) { errors ->
        errors.forEach { e -> println(e) }
      }
    }
    cancel()
  }
}
