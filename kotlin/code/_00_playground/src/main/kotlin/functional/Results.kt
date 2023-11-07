@file:Suppress("Unused", "TooGenericExceptionCaught", "MagicNumber")

package functional

import kotlin.runCatching

object Results {
  data class Job(val id: JobId, val company: Company, val role: Role, val salary: Salary)

  @JvmInline value class JobId(val value: Long)

  @JvmInline value class Company(val name: String)

  @JvmInline value class Role(val name: String)

  @JvmInline
  value class Salary(
    val value: Double
  ) { // never use Long / Double for money, as it loses precision
    operator fun compareTo(other: Salary): Int = value.compareTo(other.value)
  }

  val JOBS_DATABASE: Map<JobId, Job> =
    mapOf(
      JobId(1) to
        Job(
          JobId(1),
          Company("Apple Inc."),
          Role("Senior Software Engineer II"),
          Salary(1000000.0)
        ),
      JobId(2) to
        Job(
          JobId(2),
          Company("Microsoft Corporation"),
          Role("Software Engineer"),
          Salary(100001.0)
        ),
      JobId(3) to
        Job(
          JobId(3),
          Company("Google LLC"),
          Role("Junior Software Engineer III"),
          Salary(1000002.0)
        )
    )

  // potentially FAILED computations
  private val appleJobResult: Result<Job> =
    Result.success(
      Job(
        JobId(2),
        Company("Microsoft Corporation"),
        Role("Software Engineer II"),
        Salary(100001.0)
      )
    )

  val notFoundJob: Result<Job> = Result.failure(NoSuchElementException("Job not found"))

  private fun <T> T.toResult(): Result<T> =
    if (this is Throwable) Result.failure(this) else Result.success(this)

  val result: Result<Job> =
    Job(
        JobId(1),
        Company("Microsoft Corporation"),
        Role("Director of Engineering"),
        Salary(10000000.0)
      )
      .toResult()

  interface Jobs {
    fun findJobId(id: JobId): Result<Job?>
  }

  class TryCatchJobs : Jobs {
    override fun findJobId(id: JobId): Result<Job?> =
      try {
        Result.success(JOBS_DATABASE[id])
      } catch (e: Exception) {
        Result.failure(e)
      }
  }

  class RunCatchingJobs : Jobs {
    override fun findJobId(id: JobId): Result<Job?> = runCatching {
      JOBS_DATABASE[id] // desired value
    }
  }

  // map
  val appleJobSalary: Result<Salary> = appleJobResult.map { it.salary } // rethrowing
  val appleSalaryCatching: Result<Salary> =
    appleJobResult.mapCatching { it.salary } // encapsulation

  class CurrencyConverter {
    // throws illegal argument exception if requirement not satisfied
    fun convertUsdToEur(amount: Double?): Double =
      require(amount != null && amount >= 0.0) { "Amount must be present and positive" }
        .let { amount * 0.91 }
  }

  class JobService(private val jobs: Jobs, private val currencyConverter: CurrencyConverter) {
    fun maybePrintJobId(jobId: JobId) {
      val maybeJob: Result<Job?> = jobs.findJobId(jobId)
      // success or failure
      // getOrNull, getOrElse, ...
      if (maybeJob.isSuccess)
        maybeJob.getOrNull()?.let { println("Job found: $it") } ?: println("Job not found")
      else println("Something went wrong ${maybeJob.exceptionOrNull()}")
      // treat the exception
    }

    // you lose control over exceptions, but you can recover later
    fun getSalaryInEur(jobId: JobId): Result<Double> =
      jobs
        .findJobId(jobId)
        .map { it?.salary }
        .mapCatching { // exception swallowed and stored
          currencyConverter.convertUsdToEur(it?.value)
        }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val jobs = RunCatchingJobs()
    val currencyConverter = CurrencyConverter()
    val jobsService = JobService(jobs, currencyConverter)

    jobsService.maybePrintJobId(JobId(41)) // not found
    jobsService.maybePrintJobId(JobId(1)) // found

    val maybeSalary1 = jobsService.getSalaryInEur(JobId(42)) // failure
    println(maybeSalary1)

    val maybeSalary2 = jobsService.getSalaryInEur(JobId(2)) // success
    println(maybeSalary2)

    // recover from exceptions
    val recovered =
      maybeSalary1.recover {
        when (it) {
          is IllegalArgumentException -> println("Amount must be positive")
          else -> println("Some other error occurred: ${it.message}")
        }
        0.0 // default value
      }
    println(recovered)

    // fold
    val finalStatement =
      maybeSalary1.fold(
        { "The salary of the job is $it" },
        {
          when (it) {
            is IllegalArgumentException -> println("Amount must be positive")
            else -> println("Some other error occurred: ${it.message}")
          }
          "Job not found so we have 0.0"
        }
      )
    println(finalStatement)
  }
}
