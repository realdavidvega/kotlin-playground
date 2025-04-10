package com.rockthejvm.threads

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.thread

object ThreadsBasics {

  // Thread is an independent unit of execution
  // Thread is a data structure and maps to a native OS thread
  // Runnable is a piece of code that can be executed

  private val takingTheBus = Runnable {
    println("I'm taking the bus")
    (1..10).forEach {
      Thread.sleep(300)
      println("People boarding at stop $it")
    }
    println("Arrived at my destination :)")
  }

  private fun runThread() {
    val thread = Thread(takingTheBus)
    // thread is just data until it is started
    // runs in the background
    thread.start()
  }

  private fun runMultipleThreads() {
    val takingBus = Thread(takingTheBus)
    // same as Thread(Runnable { ... })
    val listeningToMusic = Thread {
      println("I'm listening to music")
      Thread.sleep(2000)
      println("Music is over")
    }

    // run in parallel
    // but exception if started multiple times
    takingBus.start()
    listeningToMusic.start()

    // builds starts the thread too
    thread { // we can also use start = false and start it later
      println("I'm another thread")
    }

    // join threads and wait for them to finish
    takingBus.join()
    listeningToMusic.join()
  }

  // cancel a thread or interruption
  private val scrollingSocialMedia =
    thread(start = false) {
      while (true) {
        try {
          println("Scrolling...")
          Thread.sleep(500)
        } catch (e: InterruptedException) {
          println("Oh, no! I was interrupted!")
          return@thread // non-local return
        }
      }
    }

  private fun demoCancelThread() {
    scrollingSocialMedia.start()

    // block forever!
    // scrollingSocialMedia.join()

    Thread.sleep(2000)
    // throws InterruptedException on that thread or crashing the thread
    // heavy-handling of exceptions
    scrollingSocialMedia.interrupt()
  }

  // executors
  private fun demoExecutorsAndFutures() {
    // thread pool
    // service will allow us to submit tasks
    val executor = Executors.newFixedThreadPool(10)
    // send tasks to one of the threads
    executor.submit {
      for (i in 1..10) {
        Thread.sleep(100)
        println("Processing task $i")
      }
    }

    // make a thread return a value with a future
    // it accepts a callable instead of a runnable
    val future: Future<Int> =
      executor.submit(
        // will run in one of the threads
        Callable {
          println("Computing...")
          Thread.sleep(1000)
          42
        }
      )

    // blocks the calling thread until the future is done
    println("Result is ${future.get()}")
    // similar to join but returns a value

    // shut down the executor calling it explicitly
    // wait for all tasks to finish, and no tasks may be submitted
    executor.shutdown()
  }

  @JvmStatic
  fun main(args: Array<String>) {
    // main thread
    takingTheBus.run()

    // another thread
    runThread()
    Thread.sleep(1000)
    println("Hello from main thread")

    // run multiple threads
    runMultipleThreads()

    // how to cancel a thread
    demoCancelThread()

    // managing threads by hand is clunky
    demoExecutorsAndFutures()
  }
}
