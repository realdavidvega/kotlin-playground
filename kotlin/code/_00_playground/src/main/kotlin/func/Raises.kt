@file:Suppress("Unused", "MagicNumber", "UnusedPrivateProperty")

package func

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.fold
import arrow.core.raise.recover

// 4 - arrow's raise context

object Raises {
  data class Job(val id: JobId, val company: Company, val role: Role, val salary: Salary)

  @JvmInline value class JobId(val value: Long)

  @JvmInline value class Company(val name: String)

  @JvmInline value class Role(val name: String)

  @JvmInline
  value class Salary(val value: Double) {
    operator fun compareTo(other: Salary): Int = value.compareTo(other.value)
  }

  val JOBS_DATABASE: Map<JobId, Job> =
    mapOf(
      JobId(1) to
        Job(
          JobId(1),
          Company("Apple, Inc."),
          Role("Software Engineer"),
          Salary(100_000.00),
        ),
      JobId(2) to
        Job(
          JobId(2),
          Company("Microsoft"),
          Role("Software Engineer"),
          Salary(101_000.00),
        ),
      JobId(3) to
        Job(
          JobId(3),
          Company("Google"),
          Role("Software Engineer"),
          Salary(102_000.00),
        ),
    )

  sealed interface JobError

  data class JobNotFound(val jobId: JobId) : JobError

  data class GenericError(val cause: String) : JobError

  data object NegativeSalary : JobError

  // Raise context
  interface Jobs {
    context(Raise<JobError>)
    fun findById(jobId: JobId): Job
  }

  // we can use the raise function from the DSL
  // you can only raise a type from the Raise context
  class LiveJobs : Jobs {
    context(Raise<JobError>)
    override fun findById(jobId: JobId): Job = JOBS_DATABASE[jobId] ?: raise(JobNotFound(jobId))
  }

  // the alternative without context receivers would be something like this
  interface AlternativeJobs {
    fun Raise<JobError>.findById(jobId: JobId): Job
  }

  // impl
  class AlternativeLiveJobs : AlternativeJobs {
    override fun Raise<JobError>.findById(jobId: JobId): Job =
      JOBS_DATABASE[jobId] ?: raise(JobNotFound(jobId))
  }

  // to create some raise context, we can use fold
  class JobsService(private val jobs: Jobs) {
    // creates a default raise context (DefaultRaise), and consumes it
    fun printSalaryFold(jobId: JobId): Unit =
      fold(
        // what we want to execute
        block = { jobs.findById(jobId) },
        // if we raise an error
        recover = { error: JobError ->
          when (error) {
            is JobNotFound -> println("Job with id ${jobId.value} not found")
            else -> println("An error was raised: $error")
          }
        },
        // happy path
        transform = { job: Job ->
          println("Job salary for job with id ${jobId.value} is ${job.salary}")
        },
        // this version just rethrows exceptions instead of handling them with the catch argument
        // also won't intercept serious errors: VM, interrupted (threads), cancellation (coroutines)
        // catch = { throwable: Throwable -> println("A serious error occurred: $throwable") }
      )
  }

  // would bubble up, and not converted to raise
  class CurrencyConverter {
    @Throws(IllegalArgumentException::class)
    fun convertUsdToEur(amount: Double?): Double =
      require(amount != null && amount > 0.0) { "Amount must be positive" }.let { amount * 0.91 }
  }

  // we can wrap around the converter
  class RaiseCurrencyConverter(private val currencyConverter: CurrencyConverter) {
    context(Raise<Throwable>)
    fun convertUsdToEur(amount: Double?): Double = currencyConverter.convertUsdToEur(amount)

    // if we want to bring the throwable in the context of raise
    context(Raise<JobError>)
    fun convertUsdToEur2(amount: Double?): Double =
      catch({ currencyConverter.convertUsdToEur(amount) }) { exception: IllegalArgumentException ->
        // we wrap here the throwable to our domain model and raise our domain error
        println("Oh, some error happened in the background: $exception")
        raise(NegativeSalary)
      }
  }

  class SalaryService(private val converter: RaiseCurrencyConverter) {
    fun printSalaryConvertedFold(amount: Double): Unit =
      fold(
        block = { converter.convertUsdToEur(amount) },
        catch = { ex: Throwable -> println("An exception was thrown: $ex") },
        recover = { error: Throwable -> println("An error was raised: $error") },
        transform = { salaryInEur: Double -> println("Salary in EUR: $salaryInEur") },
      )

    // recover from JobError
    // remove the catch to bubble up any exceptions
    fun printSalaryConvertedFold2(amount: Double): Unit =
      fold(
        block = { converter.convertUsdToEur2(amount) },
        recover = { error: JobError -> println("An error was raised: $error") },
        transform = { salaryInEur: Double -> println("Salary in EUR: $salaryInEur") },
      )
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val appleJobId = JobId(1)
    val jobs = LiveJobs()
    // won't work if we don't provide a Raise context
    // val myJobs = jobs.findById(appleJobId)

    // using fold for raise
    val jobService = JobsService(jobs)
    jobService.printSalaryFold(appleJobId)
    // Job salary for job with id 1 is Salary(value=100000.0)

    // another fold example
    val converter = RaiseCurrencyConverter(CurrencyConverter())
    val salaryService = SalaryService(converter)
    salaryService.printSalaryConvertedFold(-100.0)
    // An exception was thrown: java.lang.IllegalArgumentException: Amount must be positive
    salaryService.printSalaryConvertedFold2(-100.0)
    // An error was raised: NegativeSalary

    // we can also recover from errors
    val fallbackAmount = recover({ converter.convertUsdToEur2(-1.0) }) { _: JobError -> 0.0 }
    println("The fallback amount is $fallbackAmount")
    // The fallback amount is 0.0
  }
}
