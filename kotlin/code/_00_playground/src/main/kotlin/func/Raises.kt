@file:Suppress("Unused", "MagicNumber", "UnusedPrivateProperty")

package func

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.raise.Raise
import arrow.core.raise.RaiseAccumulate
import arrow.core.raise.catch
import arrow.core.raise.fold
import arrow.core.raise.mapOrAccumulate
import arrow.core.raise.recover
import arrow.core.raise.withError

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

  // for later error accumulation inside a new type
  data class JobErrors(val messages: String = "")

  // Raise context
  interface Jobs {
    context(Raise<JobError>)
    fun findById(jobId: JobId): Job

    context(Raise<JobError>)
    fun findAll(): List<Job>
  }

  // we can use the raise function from the DSL
  // you can only raise a type from the Raise context
  class LiveJobs : Jobs {
    context(Raise<JobError>)
    override fun findById(jobId: JobId): Job = JOBS_DATABASE[jobId] ?: raise(JobNotFound(jobId))

    context(Raise<JobError>)
    override fun findAll(): List<Job> =
      catch({ JOBS_DATABASE.values.toList() }) { _: Throwable ->
        raise(GenericError("An error occurred while retrieving all the jobs"))
      }
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
  class JobsService(private val jobs: Jobs, private val converter: RaiseCurrencyConverter) {
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

    // composing Raise<E> computations
    // we are only interested in the happy path in this case
    context(Raise<JobError>)
    fun getSalaryGapWithMax(jobId: JobId): Double {
      val job: Job = jobs.findById(jobId)
      val jobList: List<Job> = jobs.findAll()
      val maxSalary: Salary = jobList.maxSalary()
      return maxSalary.value - job.salary.value
    }

    // extension function with raise using context receivers
    context(Raise<JobError>)
    private fun List<Job>.maxSalary(): Salary =
      if (isEmpty()) {
        raise(GenericError("No jobs found"))
      } else {
        this.maxBy { it.salary.value }.salary
      }

    // handling logic errors from different hierarchies in the same method
    // won't compile without NegativeAmount raise context
    context(Raise<JobError>, Raise<NegativeAmount>)
    fun getSalaryGapWithMaxInEur(jobId: JobId): Double {
      val job: Job = jobs.findById(jobId)
      val jobList: List<Job> = jobs.findAll()
      val maxSalary: Salary = jobList.maxSalary()
      val salaryGap = maxSalary.value - job.salary.value
      return converter.convertUsdToEurRaisingNegativeAmount(salaryGap)
    }

    // second option, using withError
    // eventually the NegativeAmount will get converted to NegativeSalary error type and raised
    // again
    context(Raise<JobError>)
    fun getSalaryGapWithMaxInEur(jobId: JobId): Double {
      val job: Job = jobs.findById(jobId)
      val jobList: List<Job> = jobs.findAll()
      val maxSalary: Salary = jobList.maxSalary()
      val salaryGap = maxSalary.value - job.salary.value
      return withError({ NegativeSalary }) {
        converter.convertUsdToEurRaisingNegativeAmount(salaryGap)
      }
    }

    // if we want to accumulate errors inside a dedicated data structure
    // if it fails, we would get at least one error, so we can use the NonEmptyList<A> or Nel<A>
    // we can use mapOrAccumulate to accumulate errors
    // inside, it uses a dedicated RaiseAccumulate<A> context, which overrides the raise method
    // and creates a NonEmptyList<E> containing only the risen logic typed error
    context(Raise<NonEmptyList<JobError>>)
    fun getSalaryGapWithMax(jobIdList: List<JobId>): List<Double> =
      mapOrAccumulate(jobIdList) { getSalaryGapWithMax(it) }

    // what if we want directly to use the RaiseAccumulate<A> context?
    // now we can use directly the extension function over the Iterable<A> type
    context(RaiseAccumulate<JobError>)
    fun getSalaryGapWithMax(jobIdList: List<JobId>): List<Double> =
      jobIdList.mapOrAccumulate { getSalaryGapWithMax(it) }

    // we could even get an Either<Nel<E>, List<B>> instead or Raise<E> context
    fun getSalaryGapWithMaxEither(
      jobIdList: List<JobId>
    ): Either<NonEmptyList<JobError>, List<Double>> =
      jobIdList.mapOrAccumulate { getSalaryGapWithMax(it) }

    // collect errors and combine them into a custom type
    context(Raise<JobErrors>)
    fun getSalaryGapWithMaxJobErrors(jobIdList: List<JobId>) =
      mapOrAccumulate(jobIdList, ::combine) {
        withError({ jobError -> JobErrors(jobError.toString()) }) { getSalaryGapWithMax(it) }
      }

    // combining errors into a monoid
    private fun combine(one: JobErrors, other: JobErrors): JobErrors =
      JobErrors("${one.messages}, ${other.messages}")
  }

  sealed interface CurrencyConversionError

  data object NegativeAmount : CurrencyConversionError

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

    // we introduce new type of error for currencies, with new raise context
    context(Raise<NegativeAmount>)
    fun convertUsdToEurRaisingNegativeAmount(amount: Double?): Double =
      catch({ currencyConverter.convertUsdToEur(amount) }) { _: IllegalArgumentException ->
        raise(NegativeAmount)
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

    val converter = RaiseCurrencyConverter(CurrencyConverter())

    // using fold for raise
    val jobService = JobsService(jobs, converter)
    jobService.printSalaryFold(appleJobId)
    // Job salary for job with id 1 is Salary(value=100000.0)

    // another fold examples
    val salaryService = SalaryService(converter)
    salaryService.printSalaryConvertedFold(-100.0)
    // An exception was thrown: java.lang.IllegalArgumentException: Amount must be positive
    salaryService.printSalaryConvertedFold2(-100.0)
    // An error was raised: NegativeSalary

    // we can also recover from errors
    val fallbackAmount = recover({ converter.convertUsdToEur2(-1.0) }) { _: JobError -> 0.0 }
    println("The fallback amount is $fallbackAmount")
    // The fallback amount is 0.0

    // composition
    fold(
      { jobService.getSalaryGapWithMax(JobId(1)) },
      { error -> println("An error was raised: $error") },
      { salaryGap -> println("The salary gap is $salaryGap") }
    )

    // same but with errors
    fold(
      { jobService.getSalaryGapWithMax(JobId(42)) },
      { error -> println("An error was raised: $error") },
      { salaryGap -> println("The salary gap is $salaryGap") }
    )

    // composition with logic errors from different hierarchies
    fold(
      { jobService.getSalaryGapWithMaxInEur(JobId(1)) },
      { error -> println("An error was raised: $error") },
      { salaryGap -> println("The salary gap in EUR is $salaryGap") }
    )

    // same but with the errors
    fold(
      { jobService.getSalaryGapWithMaxInEur(JobId(42)) },
      { error -> println("An error was raised: $error") },
      { salaryGap -> println("The salary gap in EUR is $salaryGap") }
    )

    // accumulating errors
    fold(
      { jobService.getSalaryGapWithMax(listOf(JobId(1), JobId(2))) },
      { error -> println("The risen errors are: $error") },
      { salaryGap -> println("The list of salary gaps is $salaryGap") }
    )

    // same but with the errors
    fold(
      { jobService.getSalaryGapWithMax(listOf(JobId(1), JobId(42), JobId(-1))) },
      { error -> println("The risen errors are: $error") },
      { salaryGap -> println("The list of salary gaps is $salaryGap") }
    )

    // combining errors into a custom type
    fold(
      { jobService.getSalaryGapWithMaxJobErrors(listOf(JobId(-1), JobId(42))) },
      { error -> println("The risen errors are: $error") },
      { salaryGaps -> println("The salary gaps are $salaryGaps") }
    )
  }
}
