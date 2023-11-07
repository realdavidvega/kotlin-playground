@file:Suppress("Unused", "MagicNumber")

package functional

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
  val JOBS_DATABASE: Map<JobId, Job> =
    mapOf(
      JobId(1) to
        Job(
          JobId(1),
          Company("Apple Inc."),
          Role("Senior Software Engineer IV"),
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
          Role("Junior Software Engineer VII"),
          Salary(1000002.0)
        )
    )

  interface Jobs {
    fun findById(id: JobId): Job?

    fun findAll(): List<Job>
  }

  class JobService(private val jobs: Results.Jobs) {}

  @JvmStatic fun main(args: Array<String>) {}
}
