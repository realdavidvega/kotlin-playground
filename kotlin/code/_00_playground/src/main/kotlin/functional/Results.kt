@file:Suppress("Unused", "TooGenericExceptionCaught", "MagicNumber")

package functional

import arrow.core.flatMap
import arrow.core.raise.ensureNotNull
import arrow.core.raise.result
import kotlin.runCatching

object Results {
  data class Job(val id: JobId, val company: Company, val role: Role, val salary: Salary)

  @JvmInline value class JobId(val value: Long)

  @JvmInline value class Company(val name: String)

  @JvmInline value class Role(val name: String)

  // never use Long / Double for money, as it loses precision
  @JvmInline
  value class Salary(val value: Double) {
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
          Salary(1000001.0)
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

  val microsoftJob: Result<Job> =
    Job(
        JobId(1),
        Company("Microsoft Corporation"),
        Role("Director of Engineering"),
        Salary(10000000.0)
      )
      .toResult()

  interface Jobs {
    fun findJobId(id: JobId): Result<Job?>

    fun findAll(): Result<List<Job>>
  }

  class TryCatchJobs : Jobs {
    override fun findJobId(id: JobId): Result<Job?> =
      try {
        Result.success(JOBS_DATABASE[id])
      } catch (e: Exception) {
        Result.failure(e)
      }

    override fun findAll(): Result<List<Job>> = TODO("Not yet implemented")
  }

  class RunCatchingJobs : Jobs {
    override fun findJobId(id: JobId): Result<Job?> = runCatching {
      JOBS_DATABASE[id] // desired value
    }

    override fun findAll(): Result<List<Job>> = Result.success(JOBS_DATABASE.values.toList())
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

  fun List<Job>.maxSalary(): Result<Salary> = runCatching {
    if (this.isEmpty()) throw NoSuchElementException("No jobs present")
    else this.maxBy { it.salary.value }.salary
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

    // plain, step by step, imperative way of dealing with values (non-idiomatic way)
    // because we are manipulating the values instead of dealing with the result at a higher level
    fun getSalaryGapVsMaxNonIdiomatic(jobId: JobId): Result<Double> = runCatching {
      val maybeJob: Job? = jobs.findJobId(jobId).getOrThrow()
      val jobSalary = maybeJob?.salary ?: Salary(0.0)
      val jobList = jobs.findAll().getOrThrow()
      val maxSalary = jobList.maxSalary().getOrThrow()
      maxSalary.value - jobSalary.value
    }

    // functionally pure, chained, dealing with errors at more high level (idiomatic)
    fun getSalaryGapVsMax(jobId: JobId): Result<Double> =
      jobs.findJobId(jobId).flatMap { maybeJob -> // Job? -> Result<...>
        val salary = maybeJob?.salary ?: Salary(0.0)
        jobs.findAll().flatMap { jobList ->
          jobList.maxSalary().map { maxSalary -> maxSalary.value - salary.value }
        }
      }

    /*
     In Scala would be like that with Either/Try types
     for {
       jobs <- jobs.findById(jobId)
       salary <- job.salary?...
       jobList <- jobs.findAll()
       maxSalary <- jobList.maxSalary()
     } yield maxSalary - salary
    */

    // imperative style with arrow, short-circuit
    fun getSalaryGapVsMaxArrow(jobId: JobId): Result<Double> = result {
      // if it throws some exception, then break the chain
      val maybeJob: Job? = jobs.findJobId(jobId).bind()
      ensureNotNull(maybeJob) { NoSuchElementException("Job not found") }
      // null is eliminated by the compiler
      val jobSalary = maybeJob.salary
      val jobList = jobs.findAll().bind()
      val maxSalary = jobList.maxSalary().bind()
      maxSalary.value - jobSalary.value
      // will not be caught by the result
      // throw RuntimeException("BOOM!")
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

    fun Result<Double>.printResult() =
      this.fold({ println("Salary gap: $it") }, { println("Error: $it") })

    jobsService.getSalaryGapVsMaxNonIdiomatic(JobId(42)).printResult() // salary 0.0
    jobsService.getSalaryGapVsMax(JobId(42)).printResult() // salary 0.0
    jobsService.getSalaryGapVsMaxArrow(JobId(42)).printResult() // exception
  }
}
