@file:Suppress("Unused", "TooGenericExceptionThrown", "MagicNumber", "UnusedPrivateProperty")

package threads

import arrow.core.getOrElse
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.parZip
import arrow.fx.coroutines.resource
import arrow.fx.coroutines.resourceScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

// 6. Arrow's resources

object Resources {
  // not safe, prone to leak dataSource and userProcessor when an exception or cancellation signals
  class UserProcessor {
    fun start(): Unit = println("Creating UserProcessor")

    fun shutdown(): Unit = println("Shutting down UserProcessor")
  }

  class DataSource {
    fun connect(): Unit = println("Connecting dataSource")

    fun disconnect(): Unit = println("Closed dataSource")
  }

  class Service(val db: DataSource, val userProcessor: UserProcessor) {
    suspend fun processData(): List<String> =
      withContext(Dispatchers.IO) {
        throw RuntimeException("I'm going to leak resources by not closing them")
      }
  }

  // possible solution, but only JVM, requires closeable, cannot run suspend, boilerplate...
  class UserProcessorCloseable(val user: UserProcessor) : AutoCloseable {
    override fun close(): Unit = user.shutdown()
  }

  class DataSourceCloseable(val data: DataSource) : AutoCloseable {
    override fun close(): Unit = data.disconnect()
  }

  // resource scope to the rescue!
  // acquire + release
  private suspend fun ResourceScope.userProcessor(): UserProcessor =
    install({ UserProcessor().also { it.start() } }) { processor, _ -> processor.shutdown() }

  private suspend fun ResourceScope.dataSource(): DataSource =
    install({ DataSource().also { it.connect() } }) { dataSource, _ -> dataSource.disconnect() }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val userProcessor = UserProcessor().also { it.start() }
      val dataSource = DataSource().also { it.connect() }
      val service = Service(dataSource, userProcessor)

      // would leak resources, we are catching to avoid killing the program
      catch({ service.processData() }) { error -> println(error) }

      dataSource.disconnect()
      userProcessor.shutdown()

      println("------------------------------")

      // using autocloseable, catching again to not kill the program
      catch({
        UserProcessorCloseable(userProcessor).use { processor ->
          processor.user.start()
          DataSourceCloseable(dataSource).use { source ->
            source.data.connect()
            Service(source.data, processor.user).processData()
          }
        }
      }) { error ->
        println(error)
      }

      println("------------------------------")

      // using resource scope
      catch({
        resourceScope {
          // acquire in parallel, using parZip
          val serviceResource =
            parZip({ userProcessor() }, { dataSource() }) { processor, source ->
              Service(source, processor)
            }
          serviceResource.processData()
        }
      }) { error ->
        println(error)
      }

      // To achieve its behavior, install invokes the acquire and release step as NonCancellable.
      // If a cancellation signal or an exception is received during acquire, the resource is
      // assumed
      // to not have been acquired and thus will not trigger the release function; any composed
      // resources that are already acquired are guaranteed to release as expected.

      println("------------------------------")

      // using resource + bind to resourceScope
      val userProcessorResource: Resource<UserProcessor> =
        resource({ UserProcessor().also { it.start() } }) { processor, _ -> processor.shutdown() }

      val dataSourceResource: Resource<DataSource> =
        resource({ DataSource().also { it.connect() } }) { source, exitCase ->
          // we can also work with the exit case (why the finalizer is run)
          println("Releasing $source with exit: $exitCase")
          withContext(Dispatchers.IO) { source.disconnect() }
        }

      val serviceResource: Resource<Service> = resource {
        Service(dataSourceResource.bind(), userProcessorResource.bind())
      }

      catch({
        resourceScope {
          val data = serviceResource.bind().processData()
          println(data)
        }
      }) { error ->
        println(error)
      }

      // Resource is nothing more than a type alias for parameter-less function using ResourceScope
      // typealias Resource<A> = suspend ResourceScope.() -> A

      println("------------------------------")

      // resource for more complex scenarios that takes a block with ResourceScope as a receiver
      val userProcessorResource2: Resource<UserProcessor> = resource {
        val x: UserProcessor =
          install({ UserProcessor().also { it.start() } }, { processor, _ -> processor.shutdown() })
        x
      }

      // integration with typed errors
      // 1. when either is in the outermost position and resourceScope
      either {
          resourceScope {
            val a = install({}) { _, ex -> println("Closing A: $ex") }
            raise("Boom!")
          } // Closing A: ExitCase.Cancelled
        }
        .getOrElse { error -> println(error) } // Either.Left(Boom!)

      println("------------------------------")

      // 2. with reverse nesting order of either and resourceScope (preferred)
      resourceScope {
        either {
            val a = install({}) { _, ex -> println("Closing A: $ex") }
            raise("Boom!")
          }
          .getOrElse { error -> println(error) } // Either.Left(Boom!)
      } // Closing A: ExitCase.Completed

      // but in both cases, resources are correctly released
    }
  }
}
