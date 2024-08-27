@file:Suppress("Unused", "MagicNumber")

package language

import kotlinx.coroutines.runBlocking

/**
 * Kotlin's context receivers / context parameters
 */
object ContextReceivers {
  data class Job(val id: JobId, val company: Company, val role: Role, val salary: Salary)

  @JvmInline value class JobId(val value: Long)

  @JvmInline value class Company(val name: String)

  @JvmInline value class Role(val name: String)

  @JvmInline value class Salary(val value: Double)

  private val JOBS_DATABASE: Map<JobId, Job> =
    mapOf(
      JobId(1) to
        Job(JobId(1), Company("Apple, Inc."), Role("Software Engineer"), Salary(70_000.00)),
      JobId(2) to Job(JobId(2), Company("Microsoft"), Role("Software Engineer"), Salary(80_000.00)),
      JobId(3) to Job(JobId(3), Company("Google"), Role("Software Engineer"), Salary(90_000.00)),
    )

  // extension functions
  private fun List<Job>.toJson(): String =
    this.joinToString(separator = ", ", prefix = "[", postfix = "]") { it.toJson() }

  private fun Job.toJson(): String =
    """
        {
            "id": ${id.value},
            "company": "${company.name}",
            "role": "${role.name}",
            "salary": $salary.value}
        }
    """
      .trimIndent()

  // make it generic
  //  fun <T> List<T>.printAsJson() =
  //    this.joinToString(separator = ", ", prefix = "[", postfix = "]") {
  //      it.toJson()
  //    }
  // but we need an implementation for each

  // we define a scope
  interface JsonScope<T> { // <- dispatcher receiver
    fun T.toJson(): String // <- extension function receiver
    // 'this' type in 'toJson' function is JsonScope<T> & T
  }

  // extension function of scope
  private fun <T> JsonScope<T>.printAsJson(objs: List<T>): String =
    objs.joinToString(separator = ", ", prefix = "[", postfix = "]") { it.toJson() }

  private val jobJsonScope =
    object : JsonScope<Job> {
      override fun Job.toJson(): String {
        return """
            {
                "id": ${id.value},
                "company": "${company.name}",
                "role": "${role.name}",
                "salary": $salary.value}
            }
        """
          .trimIndent()
      }
    }

  // we are limited to one receiver
  // alternative is passing by argument or injecting it as part of a class
  interface Logger {
    fun info(message: String)
  }

  private val consoleLogger =
    object : Logger {
      override fun info(message: String) {
        println("[INFO] $message")
      }
    }

  // context receivers to the rescue!
  context(JsonScope<T>)
  private fun <T> printAsJsonOnContext(objs: List<T>): String =
    objs.joinToString(separator = ", ", prefix = "[", postfix = "]") { it.toJson() }

  // two contexts
  context(JsonScope<T>, Logger)
  private fun <T> printAsJsonWithLogger(objs: List<T>): String {
    info("Serializing $objs list as JSON")
    return objs.joinToString(separator = ", ", prefix = "[", postfix = "]") { it.toJson() }
  }

  // we can access this@Logger with that notation
  context(JsonScope<T>, Logger)
  fun <T> printAsJsonWithThis(objs: List<T>): String {
    this@Logger.info("Serializing $objs list as JSON")
    return objs.joinToString(separator = ", ", prefix = "[", postfix = "]") { it.toJson() }
  }

  // we can have multiple functions with same name, but would have different signature
  // public static final <T> String printAsJson(JsonScope<T> jsonScope, Logger logger, List<T>
  // objs)

  interface Jobs {
    suspend fun findById(id: JobId): Job?
  }

  // impl
  context(Logger)
  class LiveJobs : Jobs {
    override suspend fun findById(id: JobId): Job? {
      info("Searching job with id $id")
      return JOBS_DATABASE[id]
    }
  }

  // context receivers are suitable for dependency injection
  context(Jobs, JsonScope<Job>, Logger)
  class JobController {
    suspend fun findJobById(id: String): String {
      info("Searching job with id $id")
      val jobId = JobId(id.toLong())
      return findById(jobId)?.let {
        info("Job with id $id found")
        return it.toJson()
      } ?: "No job found with id $id"
    }
  }

  // more explicit, but passing business logic as context
  context(Jobs, JsonScope<Job>, Logger)
  class JobControllerExplicit {
    suspend fun findJobById(id: String): String {
      this@Logger.info("Searching job with id $id")
      val jobId = JobId(id.toLong())
      return this@Jobs.findById(jobId)?.let {
        this@Logger.info("Job with id $id found")
        return it.toJson()
      } ?: "No job found with id $id"
    }
  }

  // in overall, is better practice to pass business logic algebras explicitly.
  // meaning, we should inject it through constructor of the class instead.
  // that way, the responsibilities of each method call are clear and explicit.
  context(JsonScope<Job>, Logger)
  class JobControllerGreat(private val jobs: Jobs) {
    suspend fun findJobById(id: String): String {
      info("Searching job with id $id")
      val jobId = JobId(id.toLong())
      return jobs.findById(jobId)?.let {
        info("Job with id $id found")
        return it.toJson()
      } ?: "No job found with id $id"
    }
  }

  /* how it looks in Scala
    class JobController[F[_]: Monad: Jobs: JsonScope: Logger]: F[String] {
    def findJobById(id: String): F[String] = {
      Logger[F].info(s"Searching job with id $id") *>
      Jobs[F].findById(JobId(id.toLong)).flatMap {
        case Some(job) =>
        Logger[F].info(s"Job with id $id found") *>
        job.toJson.pure[F]
        case None =>
        s"No job found with id $id".pure[F]
      }
    }
  }

  object Jobs {
    def apply[F[_]](implicit jobs: Jobs[F]): Jobs[F] = jobs
  }
   */

  @JvmStatic
  fun main(args: Array<String>) {
    println(JOBS_DATABASE.values.toList().toJson())

    with(jobJsonScope) { println(printAsJson(JOBS_DATABASE.values.toList())) }

    // we can't restrict that...
    jobJsonScope.printAsJson(JOBS_DATABASE.values.toList())

    // works
    with(jobJsonScope) { println(printAsJsonOnContext(JOBS_DATABASE.values.toList())) }

    // won't compile as it is not inside the JsonScope
    // jobJsonScope.printAsJsonOnContext(JOBS_DATABASE.values.toList())

    // both contexts
    with(jobJsonScope) {
      with(consoleLogger) { println(printAsJsonWithLogger(JOBS_DATABASE.values.toList())) }
    }

    // coroutine context
    runBlocking {
      with(consoleLogger) {
        val jobs = LiveJobs()
        jobs.findById(JobId(1))?.toJson().also(::println)
      }

      // we must provide the two contexts in order to use the controller
      with(jobJsonScope) {
        with(consoleLogger) {
          JobControllerGreat(jobs = LiveJobs()).findJobById("1").also(::println)
        }
      }
    }
  }
}
