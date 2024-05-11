@file:Suppress("Unused", "MagicNumber", "UnusedPrivateProperty")

package functional

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.raise.NullableRaise
import arrow.core.raise.OptionRaise
import arrow.core.raise.Raise
import arrow.core.raise.ResultRaise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.fold
import arrow.core.raise.nullable
import arrow.core.raise.option
import arrow.core.raise.result
import functional.Raises.Company
import functional.Raises.CurrencyConverter
import functional.Raises.JOBS_DATABASE
import functional.Raises.Job
import functional.Raises.JobError
import functional.Raises.JobId
import functional.Raises.Role
import functional.Raises.Salary

// 5. Arrow's raises to wrappers

object RaisesToWrappers {
  interface Jobs {
    // for either
    context(Raise<JobError>)
    fun findById(jobId: JobId): Job

    // for option
    context(Raise<None>)
    fun findByIdWithOption(id: JobId): Job

    // for nullable
    context(NullableRaise)
    fun findByIdWithNullable(id: JobId): Job
  }

  class LiveJobs : Jobs {
    // for either
    context(Raise<JobError>)
    override fun findById(jobId: JobId): Job =
      JOBS_DATABASE[jobId] ?: raise(Raises.JobNotFound(jobId))

    // for option
    context(Raise<None>)
    override fun findByIdWithOption(id: JobId): Job = JOBS_DATABASE[id] ?: raise(None)

    // for nullable
    context(NullableRaise)
    override fun findByIdWithNullable(id: JobId): Job = JOBS_DATABASE[id] ?: raise(null)
  }

  class JobsService(private val jobs: Jobs) {
    // convert to either
    // the builder creates the context of type Raise<JobError>, handling it properly
    fun company(jobId: JobId): Either<JobError, Company> = either { jobs.findById(jobId).company }

    // conversion from an Either<E, A> to a Raise<E>
    context(Raise<JobError>)
    fun companyWithRaise(jobId: JobId): Company = company(jobId).bind()

    // convert to option
    // works very similarly to the 'either' builder
    fun salary(jobId: JobId): Option<Salary> = option {
      // block must be in the context of OptionRaise = Raise<None>
      jobs.findByIdWithOption(jobId).salary
    }

    // conversion from Option<A> to Raise<None>
    context(OptionRaise)
    fun salaryWithRaise(jobId: JobId): Salary = salary(jobId).bind()

    // we can also convert from option to nullable
    context(NullableRaise)
    fun salaryWithNullableRaise(jobId: JobId): Salary = salary(jobId).bind()

    // convert to nullable
    fun role(jobId: JobId): Role? = nullable { jobs.findByIdWithNullable(jobId).role }

    // conversion from nullable object to a NullableRaise
    context(NullableRaise)
    fun roleWithRaise(jobId: JobId): Role = role(jobId).bind()
  }

  class RaiseCurrencyConverter(private val currencyConverter: CurrencyConverter) {
    // with the Result<A> wrapper type
    context(ResultRaise)
    fun convertUsdToEurRaiseException(amount: Double?): Double =
      catch({ currencyConverter.convertUsdToEur(amount) }) { ex: IllegalArgumentException ->
        raise(ex)
      }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val jobs = LiveJobs()
    val jobsService = JobsService(jobs)

    // company with either
    jobsService
      .company(JobId(42))
      .map { println("The company name is: ${it.name}") }
      .getOrElse { error -> println("An error was raised (for company): $error") }

    // company with raise
    fold(
      block = { jobsService.companyWithRaise(JobId(42)) },
      recover = { error: JobError -> println("An error was raised (for company): $error") },
      transform = { company: Company -> println("The company name is: ${company.name}") },
    )

    // salary with option
    jobsService
      .salary(JobId(42))
      .map { println("The salary is: ${it.value}") }
      .getOrElse { println("An error was raised (for salary)") }

    // salary with raise
    option { jobsService.salaryWithRaise(JobId(42)) }
      .map { println("The salary is: ${it.value}") }
      .getOrElse { println("An error was raised (for salary)") }

    // role with nullable
    jobsService.role(JobId(42))?.let { println("The role is: ${it.name}") }
      ?: println("An error was raised (for role)")

    // role with raise
    nullable { jobsService.roleWithRaise(JobId(42)) }?.let { println("The role is: ${it.name}") }
      ?: println("An error was raised (for role)")

    // with result
    val converter = RaiseCurrencyConverter(CurrencyConverter())
    val maybeSalaryInEur: (Double) -> Result<Double> = { salary: Double ->
      result { converter.convertUsdToEurRaiseException(salary) }
    }

    maybeSalaryInEur(100.0)
      .getOrElse { throwable: Throwable -> println("An error occurred: $throwable") }
      .let { println("The salary in EUR is: $it") }

    // also we can convert it back to raise
    //    val maybeSalaryInEurRaise: context(ResultRaise) (Double) -> Double = { salary: Double ->
    //      maybeSalaryInEur(salary).bind()
    //    }
    //
    //    result {
    //      maybeSalaryInEurRaise(this, 100.0)
    //    }.getOrElse { throwable: Throwable ->
    //      println("An error occurred: $throwable")
    //    }.let { println("The salary in EUR is: $it") }
  }
}
