@file:Suppress("Unused", "MagicNumber", "UnusedPrivateProperty", "TooGenericExceptionCaught")

package functional

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.right

// 3. Arrow's eithers

object Eithers {
  data class Job(val id: JobId, val company: Company, val role: Role, val salary: Salary)

  @JvmInline value class JobId(val value: Long)

  @JvmInline value class Company(val name: String)

  @JvmInline value class Role(val name: String)

  @JvmInline
  value class Salary(val value: Double) {
    operator fun compareTo(other: Salary): Int = value.compareTo(other.value)
  }

  // database
  private val JOBS_DATABASE: Map<JobId, Job> =
    mapOf(
      JobId(1) to
        Job(
          JobId(1),
          Company("Apple Inc."),
          Role("Senior Software Engineer IV"),
          Salary(1000000.0),
        ),
      JobId(2) to
        Job(
          JobId(2),
          Company("Microsoft Corporation"),
          Role("Software Engineer"),
          Salary(1000001.0),
        ),
      JobId(3) to
        Job(
          JobId(3),
          Company("Google LLC"),
          Role("Junior Software Engineer VII"),
          Salary(1000002.0),
        ),
    )

  interface Jobs {
    fun findById(id: JobId): Either<JobError, Job>

    fun findAll(): Either<JobError, List<Job>>
  }

  class LiveJobs : Jobs {
    override fun findById(id: JobId): Either<JobError, Job> = findByIdV3(id)

    override fun findAll(): Either<JobError, List<Job>> = JOBS_DATABASE.values.toList().right()
  }

  fun findById(id: JobId): Either<JobError, Job> =
    try {
      JOBS_DATABASE[id]?.right() ?: JobError.NotFound(id).left()
    } catch (e: Exception) {
      JobError.Generic(e.message ?: "Unknown error").left()
    }

  // Monadic style
  fun findByIdV2(id: JobId): Either<JobError, Job> =
    Either.catch { JOBS_DATABASE[id] }
      .mapLeft { JobError.Generic(it.message ?: "Unknown error") }
      .flatMap { maybeJob -> maybeJob?.right() ?: JobError.NotFound(id).left() }

  fun findByIdV3(id: JobId): Either<JobError, Job> =
    catch({ JOBS_DATABASE[id]?.right() ?: JobError.NotFound(id).left() }) { e: Exception ->
      JobError.Generic(e.message ?: "Unknown error").left()
    }

  sealed interface JobError {
    data class NotFound(val jobId: JobId) : JobError

    data class Generic(val cause: String) : JobError
  }

  // Either type
  private val appleJobEither: Either<JobError, Job> = Either.Right(JOBS_DATABASE[JobId(1)]!!)
  private val notFoundEither: Either<JobError, Job> = Either.Left(JobError.NotFound(JobId(41)))

  // check isLeft, isRight
  private val isRight = appleJobEither.isRight()
  private val isLeft =
    notFoundEither.isLeft { it is JobError.Generic && it.cause.contains("stuff") }

  // extension functions
  private val appleJobEitherV2: Either<JobError, Job> = JOBS_DATABASE[JobId(1)]!!.right()
  private val notFoundEitherV2: Either<JobError, Job> = JobError.NotFound(JobId(41)).left()

  // getOrElse, getOrNull...
  private val appleJobValue: Job? = appleJobEitherV2.getOrNull()
  private val notFoundValue: Job =
    appleJobEitherV2.getOrElse {
      Job(
        JobId(0),
        Company("Default Company"),
        Role("Default Software Engineer Role"),
        Salary(1000000.0),
      )
    }

  // map, flatMap
  private val appleJobSalary: Salary? = appleJobEitherV2.map { it.salary }.getOrNull()

  fun List<Job>.maxSalary(): Either<JobError, Salary> =
    if (this.isEmpty()) JobError.Generic("No jobs present").left()
    else this.maxBy { it.salary.value }.salary.right()

  // Monadic style
  class JobService(private val jobs: Jobs) {
    // salary gap vs max with Either type
    fun getSalaryGapVsMax(jobId: JobId): Either<JobError, Double> =
      jobs.findById(jobId).flatMap { job -> // Job -> Either<...>
        val salary = job.salary // generic error short-circuits
        jobs.findAll().flatMap { jobsList ->
          jobsList.maxSalary().map { maxSalary -> maxSalary.value - salary.value }
        }
      }

    /*
     Similar to Scala
     for {
       job <- jobs.findById(...)
       jobList <- jobs.findAll
       maxSalary <- jobList.maxSalary()
     } yield maxSalary - job.salary
    */
    fun getSalaryGapVsMaxArrow(jobId: JobId): Either<JobError, Double> = either {
      val job: Job = jobs.findById(jobId).bind() // short-circuits, break the chain, if throws
      val jobSalary = job.salary
      val jobList = jobs.findAll().bind()
      val maxSalary = jobList.maxSalary().bind()
      maxSalary.value - jobSalary.value
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val jobs = LiveJobs()
    val jobsService = JobService(jobs)

    fun Either<JobError, Double>.printResult(): Unit =
      this.fold({ println("Salary gap: $it") }, { println("Error: $it") })

    println(jobsService.getSalaryGapVsMax(JobId(42)))
    println(jobsService.getSalaryGapVsMaxArrow(JobId(42)))
  }
}
