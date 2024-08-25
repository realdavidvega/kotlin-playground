@file:Suppress("Unused", "MagicNumber", "UnusedPrivateProperty")

package language

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either

/**
 * Kotlin's operator overloading and invoke
 */
object Operators {
  sealed interface SalaryError {
    data object InvalidAmount : SalaryError

    data object InvalidCurrency : SalaryError

    data object IncompatibleCurrencies : SalaryError

    data class NotFound(val salaryId: Long) : SalaryError
  }

  @JvmInline
  value class Amount(val value: Double) {
    operator fun plus(other: Amount): Amount = Amount(this.value + other.value)
  }

  @JvmInline value class Currency(val value: String)

  data class Salary(val amount: Amount, val currency: Currency) {
    context(Raise<SalaryError>)
    operator fun plus(other: Salary): Salary =
      if (this.currency == other.currency) Salary(this.amount + other.amount, this.currency)
      else raise(SalaryError.IncompatibleCurrencies)

    context(Raise<SalaryError>)
    infix fun isGreaterThan(other: Salary): Boolean =
      if (this.currency == other.currency) this.amount.value > other.amount.value
      else raise(SalaryError.IncompatibleCurrencies)
  }

  val SALARY_DATABASE =
    mapOf(
      1L to Salary(Amount(500.0), Currency("EUR")),
      2L to Salary(Amount(1500.0), Currency("EUR")),
      3L to Salary(Amount(2000.0), Currency("USD")),
    )

  interface Salaries {
    context(Raise<SalaryError.NotFound>)
    fun findById(salaryId: Long): Salary

    companion object {
      operator fun invoke(): Salaries =
        object : Salaries {
          context(Raise<SalaryError.NotFound>)
          override fun findById(salaryId: Long): Salary =
            SALARY_DATABASE[salaryId] ?: raise(SalaryError.NotFound(salaryId))
        }
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val salaryOne = Salary(Amount(500.0), Currency("EUR"))
    val salaryTwo = Salary(Amount(1500.0), Currency("EUR"))
    val salaryThree = Salary(Amount(2000.0), Currency("USD"))

    // either block for raise
    either {
        // operator function
        salaryOne + salaryTwo + salaryThree
      }
      .map { (amount, currency) ->
        println("The total income is: ${amount.value} ${currency.value}")
      }
      .getOrElse { println("The salary currencies are incompatible.") }

    // either block for raise
    either {
        // infix function
        salaryTwo isGreaterThan salaryOne
      }
      .map { isGreater -> if (isGreater) println("Yes, the first is greater than the second.") }
      .getOrElse { println("The salary currencies are incompatible.") }

    // invoke operator function
    val salaries = Salaries()
    val salaryOneId = 1L

    either { salaryOneId to salaries.findById(salaryOneId) }
      .map { (id, salary) ->
        println("The salary with id $id is ${salary.amount.value} ${salary.currency.value}")
      }
  }
}
