@file:Suppress("MagicNumber", "SwallowedException", "TooGenericExceptionCaught", "Unused")

package functional

import arrow.core.Option
import arrow.core.none
import arrow.core.raise.nullable
import arrow.core.raise.option
import arrow.core.toOption
import java.util.NoSuchElementException

object Options {
  data class Job(val id: JobId, val company: Company, val role: Role, val salary: Salary)

  @JvmInline value class JobId(val value: Long)

  @JvmInline value class Company(val name: String)

  @JvmInline value class Role(val name: String)

  @JvmInline
  value class Salary(val value: Double) {
    operator fun compareTo(other: Salary): Int = value.compareTo(other.value)
  }

  // database
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
          Role("Software Engineer II"),
          Salary(1000001.0)
        ),
      JobId(3) to
        Job(
          JobId(3),
          Company("Google LLC"),
          Role("Junior Software Engineer IV"),
          Salary(10000002.0)
        )
    )

  interface Jobs {
    fun findById(id: JobId): Job? // ? means that this value may be null

    fun findByIdOption(id: JobId): Option<Job>

    fun findAll(): List<Job>
  }

  // throw exception if the input is invalid
  class NaiveJobs : Jobs {
    override fun findById(id: JobId): Job {
      val maybeJob: Job? = JOBS_DATABASE[id]
      if (maybeJob != null) return maybeJob else throw NoSuchElementException("Job not found")
    }

    override fun findByIdOption(id: JobId): Option<Job> = TODO("Not yet implemented")

    override fun findAll(): List<Job> = TODO("Not yet implemented")
  }

  // nullables
  class LiveJobs : Jobs {
    override fun findById(id: JobId): Job? =
      try {
        JOBS_DATABASE[id]
      } catch (e: Exception) {
        null
      }

    override fun findByIdOption(id: JobId): Option<Job> =
      try {
        JOBS_DATABASE[id].toOption()
      } catch (e: Exception) {
        none()
      }

    override fun findAll(): List<Job> = JOBS_DATABASE.values.toList()
  }

  //  class JobsService(private val jobs: Jobs) {
  //    fun retrieveSalary(id: JobId): Double {
  //      val job = jobs.findById(id)
  //      return try {
  //        job.salary.value
  //      } catch (e: Exception) {
  //        0.0
  //      }
  //    }
  //  }

  // referential transparency
  //  class JobsService2(private val jobs: Jobs) {
  //    fun retrieveSalary(id: JobId): Double {
  //      val job = throw NoSuchElementException("Job not found")
  //      return try {
  //        job.salary.value
  //      } catch (e: Exception) {
  //        0.0
  //      }
  //    }
  //  }

  class JobsService3(private val jobs: Jobs, private val converter: CurrencyConverter) {
    fun retrieveSalary(id: JobId): Double = jobs.findById(id)?.salary?.value ?: 0.0

    // Options(jobs.findById(id)).map((j: Job) => converter.usd2Eur(j.salary.value)).getOrElse(0.0)
    fun retrieveSalaryEur(id: JobId): Double =
      jobs.findById(id)?.let { // lambda
        converter.usd2Eur(it.salary.value)
      }
        ?: 0.0

    fun isFromCompany(id: JobId, company: String): Boolean =
      jobs.findById(id)?.takeIf { it.company.name == company } != null

    // nested
    fun sumSalaries(jobId1: JobId, jobId2: JobId): Double? {
      val maybeJob1: Job? = jobs.findById(jobId1)
      val maybeJob2: Job? = jobs.findById(jobId2)
      return maybeJob1?.let { job1 ->
        maybeJob2?.let { job2 -> job1.salary.value + job2.salary.value }
      }
    }

    // Arrow / nullable
    fun sumSalaries2(jobId1: JobId, jobId2: JobId): Double? = nullable {
      println("Searching for job $jobId1")
      val job1: Job = jobs.findById(jobId1).bind()
      println("Job 1 found: $job1")
      println("Searching for job $jobId2")
      val job2: Job = jobs.findById(jobId2).bind()
      job1.salary.value + job2.salary.value
    }

    // nested
    fun salaryGapVsMax(jobId: JobId): Option<Double> {
      val maybeJob: Option<Job> = jobs.findByIdOption(jobId)
      val maybeMaxSalary: Option<Salary> =
        jobs.findAll().maxBy { it.salary.value }.toOption().map { it.salary }
      return maybeJob.flatMap { job ->
        maybeMaxSalary.map { maxSalary -> maxSalary.value - job.salary.value }
      }
    }

    // Arrow / option
    fun salaryGapVsMax2(jobId: JobId): Option<Double> = option {
      println("Searching for job $jobId")
      val job: Job = jobs.findByIdOption(jobId).bind()
      println("Job found: $job")
      println("Searching for max salary job")
      val maxSalaryJob: Job = jobs.findAll().maxBy { it.salary.value }.toOption().bind()
      println("Max salary job found: $maxSalaryJob")
      maxSalaryJob.salary.value - job.salary.value
    }
  }

  // functions that throw exceptions are NOT RT
  // we want the compiler to warn us of potential errors
  // "checked" exceptions don't work well with FP (HOFs)
  // List(1,2,3).map(x => x + 1)

  // nullable types
  // let
  class CurrencyConverter {
    fun usd2Eur(amount: Double): Double = amount * 0.91
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val jobs: Jobs = LiveJobs()
    val currencyConverter = CurrencyConverter()
    val jobsService = JobsService3(jobs, currencyConverter)
    val jobId: Long = 1
    val isAppleJob = jobsService.isFromCompany(JobId(jobId), "Apple")
    println("Job is $jobId is${if (isAppleJob) "" else " NOT"} from Apple")
    val salary = jobsService.retrieveSalary(JobId(jobId))
    println("Salary of the job $jobId is $salary")
    val sumSalaries = jobsService.sumSalaries2(JobId(1), JobId(42)) ?: 0.0
    println("Sum of salaries of jobs: $sumSalaries")
    println("Testing option examples...")
    val salaryDiff = jobsService.salaryGapVsMax(JobId(1)) // 2
    println("Salary diff V1: $salaryDiff")
    val salaryDiffNone = jobsService.salaryGapVsMax(JobId(42)) // 2
    println("Salary diff none V1: $salaryDiffNone")
    val salaryDiff2 = jobsService.salaryGapVsMax2(JobId(1)) // 2
    println("Salary diff V2: $salaryDiff2")
    val salaryDiffNone2 = jobsService.salaryGapVsMax2(JobId(42)) // 2
    println("Salary diff none V2: $salaryDiffNone2")
  }
}
