@file:Suppress("Unused", "MagicNumber")

package structs

import arrow.core.firstOrNone
import arrow.core.getOrElse

object Lists {
  @JvmInline value class JobId(val value: Long)

  @JvmInline value class Company(val name: String)

  @JvmInline value class Role(val name: String)

  data class Job(val id: JobId, val company: Company, val role: Role, val salary: Salary)

  @JvmInline
  value class Salary(val value: Double) {
    operator fun plus(other: Salary): Salary = Salary(value + other.value)

    operator fun compareTo(other: Salary): Int = value.compareTo(other.value)
  }

  fun main() {
    val jobs: List<Job> =
      listOf(
        Job(JobId(1), Company("Apple"), Role("Data Engineer"), Salary(100000.0)),
        Job(JobId(2), Company("Microsoft"), Role("Software Engineer"), Salary(100001.0)),
        Job(JobId(3), Company("Google"), Role("Site Reliability Engineer"), Salary(100002.0)),
        Job(JobId(3), Company("Apple"), Role("Product Owner"), Salary(60000.0))
      )

    val aggregatedEngineerSalary: Double =
      jobs
        .filter { it.role.name.contains("Engineer") }
        .map { it.salary }
        .reduce { acc, salary -> acc + salary }
        .value

    println("Aggregated engineer salary 1: $aggregatedEngineerSalary")

    val aggregatedEngineerSalary2: Double =
      jobs
        .filter { it.role.name.contains("Engineer") }
        .fold(0.0) { acc, job -> acc + job.salary.value }

    println("Aggregated engineer salary 2: $aggregatedEngineerSalary2")

    val aggregatedEngineerSalary3: Double =
      jobs.filter { it.role.name.contains("Engineer") }.sumOf { it.salary.value }

    println("Aggregated engineer salary 3: $aggregatedEngineerSalary3")

    val productOwnerSalaryValueOrZero =
      jobs.firstOrNull { it.role.name == "Product Owner" }?.salary?.value ?: 0.0

    println("Product owner salary or zero: $productOwnerSalaryValueOrZero")

    val productOwnerSalaryValueOrOne =
      jobs.firstOrNone { it.role.name == "Product Owner" }.fold({ 1.0 }, { it.salary.value })

    println("Product owner salary or one 1: $productOwnerSalaryValueOrOne")

    val productOwnerSalaryValueOrOne2 =
      jobs.firstOrNone { it.role.name == "Product Owner" }.map { it.salary.value }.getOrElse { 1.0 }

    println("Product owner salary or one 2: $productOwnerSalaryValueOrOne2")
  }
}
