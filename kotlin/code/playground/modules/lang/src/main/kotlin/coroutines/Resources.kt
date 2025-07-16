@file:Suppress("Unused", "TooGenericExceptionThrown", "MagicNumber", "UnusedPrivateProperty")

package coroutines

import arrow.autoCloseScope
import arrow.core.getOrElse
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.autoCloseable
import arrow.fx.coroutines.closeable
import arrow.fx.coroutines.parZip
import arrow.fx.coroutines.resource
import arrow.fx.coroutines.resourceScope
import arrow.fx.coroutines.use
import java.io.Closeable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * (5) Resources
 *
 * Resource and closeable using `arrow.fx.coroutines`.
 */
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
  class UserProcessorCloseable(val user: UserProcessor) : Closeable {
    override fun close(): Unit = user.shutdown()
  }

  class DataSourceCloseable(val data: DataSource) : Closeable {
    override fun close(): Unit = data.disconnect()
  }

  // possible solution, but only JVM, requires closeable, cannot run suspend, boilerplate...
  class UserProcessorAutoCloseable(val user: UserProcessor) : AutoCloseable {
    override fun close(): Unit = user.shutdown()
  }

  class DataSourceAutoCloseable(val data: DataSource) : AutoCloseable {
    override fun close(): Unit = data.disconnect()
  }

  class ServiceAutoCloseable(
    val dataSourceCloseable: DataSourceAutoCloseable,
    val userProcessorCloseable: UserProcessorAutoCloseable,
  ) {
    suspend fun processData(): List<String> =
      withContext(Dispatchers.IO) { throw RuntimeException("I'm going to do some stuff") }
  }

  // resource scope to the rescue!
  // acquire + release
  private suspend fun ResourceScope.userProcessor(): UserProcessor =
    install({ UserProcessor().also { it.start() } }) { processor, _ -> processor.shutdown() }

  private suspend fun ResourceScope.dataSource(): DataSource =
    install({ DataSource().also { it.connect() } }) { dataSource, _ -> dataSource.disconnect() }

  @OptIn(ExperimentalStdlibApi::class)
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

      // Using Autocloseable, catching again to not kill the program
      catch({
        UserProcessorAutoCloseable(userProcessor).use { processor ->
          processor.user.start()
          DataSourceAutoCloseable(dataSource).use { source ->
            source.data.connect()
            Service(source.data, processor.user).processData()
          }
        }
      }) { error ->
        println(error)
      }

      // Using arrow's autoCloseScope block
      // This way we can avoid having a callback hell in the code
      catch({
        autoCloseScope {
          val processor = install(UserProcessorAutoCloseable(userProcessor))
          processor.user.start()

          val source = install(DataSourceAutoCloseable(dataSource))
          source.data.connect()
          Service(source.data, processor.user).processData()
        }
      }) {
        println(it)
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
      // assumed to not have been acquired and thus will not trigger the release function;
      // any composed resources that are already acquired are guaranteed to release as expected.

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

      // We can also use closeable, which returns a Resource from a Closable
      // It uses Closeable.close() as the release function
      val userProcessorResource3 = closeable { UserProcessorCloseable(userProcessor) }
      val dataSourceResource3 = closeable { DataSourceCloseable(dataSource) }

      // We also have autoClosable, which returns a Resource from AutoCloseable
      // It uses AutoCloseable.close() as the release function
      val userProcessorResource4 = autoCloseable { UserProcessorAutoCloseable(userProcessor) }
      val dataSourceResource4 = autoCloseable { DataSourceAutoCloseable(dataSource) }

      // We can combine them into a single resource if needed
      val serviceResource2 = resource {
        ServiceAutoCloseable(dataSourceResource4.bind(), userProcessorResource4.bind())
      }

      // And use it in the resourceScope
      suspend fun callServiceResource() {
        resourceScope {
          // with use, we can use the resource in the scope
          serviceResource2.use { it.processData() }
          // imperative style
          serviceResource2.bind().processData()
        }
      }
    }
  }
}
