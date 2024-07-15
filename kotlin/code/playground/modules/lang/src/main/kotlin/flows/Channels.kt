package flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// 8. Channels

object Channels {
  @OptIn(ExperimentalCoroutinesApi::class)
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // Deferred values provide a convenient way to transfer a single value between coroutines.
      // Channels provide a way to transfer a stream of values.

      // A Channel is conceptually very similar to BlockingQueue. One key difference is that
      // instead of a blocking put operation it has a suspending send, and instead of a blocking
      // take operation it has a suspending receive.
      val actors = Channel<Flows.Actor>()
      launch {
        listOf(Flows.henryCavill, Flows.galGadot, Flows.ezraMiller).forEach {
          delay(250)
          actors.send(it)
        }
      }

      // Receive values from the channel with 'receive', executing it five times
      repeat(3) { println(actors.receive()) } // It will wait until a value is sent
      println("Done!")
      println("------------------------------")

      // We can also close the channel, indicating that no more values will be sent
      // Conceptually, a close is like sending a special close token to the channel.
      // The iteration stops as soon as this close token is received, so there is a guarantee
      // that all previously sent elements before the close are received:
      val someOtherActors = Channel<Flows.Actor>()
      launch {
        listOf(Flows.benAffleck, Flows.jasonMomoa).forEach {
          delay(250)
          someOtherActors.send(it)
        }
        someOtherActors.close() // We're done sending
      }

      // We print received values using `for` loop (until the channel is closed)
      for (actor in someOtherActors) println(actor)
      println("Done!")
      println("------------------------------")

      // We can also build a channel as a producer and a consumer that is often found in concurrent
      // code.

      // There is a convenient coroutine builder named produce that makes it easy to do it right
      // on producer side
      fun CoroutineScope.produceActors(): ReceiveChannel<Flows.Actor> = produce {
        listOf(Flows.henryCavill, Flows.galGadot, Flows.ezraMiller).forEach {
          delay(250)
          send(it)
        }
      }

      // An extension function consumeEach, that replaces a for loop on the consumer side
      val actorsProduced = produceActors()
      actorsProduced.consumeEach { println(it) }
      println("Done!")
      println("------------------------------")

      // The channels shown so far had no buffer. Unbuffered channels transfer elements when
      // sender and receiver meet each other (aka rendezvous). If send is invoked first, then
      // it is suspended until receive is invoked, if receive is invoked first, it is suspended
      // until send is invoked.

      // Both Channel() factory function and produce builder take an optional capacity parameter
      // to specify buffer size. Buffer allows senders to send multiple elements before suspending,
      // similar to the BlockingQueue with a specified capacity, which blocks when buffer is full.
      val bufferedActors = Channel<Flows.Actor>(capacity = 1)
      val sender = launch {
        listOf(Flows.henryCavill, Flows.galGadot, Flows.ezraMiller).forEach {
          println("Sending $it") // print before sending each element
          bufferedActors.send(it) // will suspend when buffer is full
          delay(100)
        }
      }
      // Don't receive anything... just wait....
      delay(1000)
      sender.cancel() // cancel sender coroutine
      bufferedActors.close() // close channel to indicate that no more values will be sent

      bufferedActors.consumeEach { println("Received $it") }
      println("Done!")
      println("------------------------------")

      // Also, send and receive operations to channels are fair with respect to the order of their
      // invocation from multiple coroutines. They are served in first-in first-out order,
      // e.g. the first coroutine to invoke receive gets the element

      // What if we want to use channels in a Flow? We can use the ChannelFlow builder
      // First, let's take a look into what is the channelFlow builder
      val actorsChannelFlow = channelFlow {
        listOf(Flows.henryCavill, Flows.galGadot, Flows.ezraMiller).forEach {
          // instead of emit, we use send
          send(it)
        }
      }

      // Nothing new here, we just created something like a flow, which we can also consume
      actorsChannelFlow.onEach { delay(250) }.collect { println(it) }
      println("------------------------------")

      // channelFlow allows doing concurrent decomposition, meaning they break down into
      // multiple different coroutines that produce different values.

      // You can't do this in flow, if you do so, you will get an IllegalStateException
      // Flow invariant is violated: Emission from another coroutine is detected
      // FlowCollector is not thread-safe and concurrent emissions are prohibited
      val multipleActors = channelFlow {
        launch { send(Flows.henryCavill) }
        launch { send(Flows.robertDowneyJr) }
      }

      println(multipleActors.toList().joinToString())
      println("------------------------------")

      // A flow is pretty similar to a suspend function. It’s a control structure containing a
      // reusable chunk of asynchronous computation that can be executed by a coroutine.
      // And because a flow is always executed by a coroutine, it’s allowed to call
      // other suspending functions

      // The difference between a flow and a suspend function is just the number of values
      // they produce

      val myFavoriteActor = Channel<Flows.Actor>(1)

      // You can't do this in flow either
      val actorsChannelFlow_v2 = channelFlow {
        listOf(Flows.henryCavill, Flows.galGadot, Flows.robertDowneyJr).forEach {
          launch {
            if (it == Flows.robertDowneyJr) myFavoriteActor.send(it)
            send(it)
          }
        }
      }
      actorsChannelFlow_v2.collect { println("All actors: $it") }
      println("My favorite actor: ${myFavoriteActor.receive()}")
      println("------------------------------")

      // Actually, under the hood, a channel flow uses a Channel to pass the values from the
      // background coroutines back to the main control flow. When a background coroutine
      // wants to emit a value, it sends the value to the channel

      // These foreground flow emissions are happening in the same single control flow that is
      // collecting the flow, so they don’t violate the flow’s constraints and are guaranteed never
      // to happen concurrently. Meanwhile, the background coroutines can execute concurrently with
      // both the flow collector and the flow producer, since they never attempt to interact with
      // the flow collector directly.
      fun myFlowChannel() = flow {
        val output = Channel<Flows.Actor>()
        coroutineScope {
          launch {
            delay(100)
            output.send(Flows.chrisEvans)
          }
          launch {
            delay(200)
            output.send(Flows.chrisHemsworth)
          }
          emitAll(output)
        }
      }

      myFlowChannel().collect { println(it) }
      println("------------------------------")

      // If we extract the launch block to a lambda, we get this simplified version of
      // channelFlow (missing some features like closing the channel)
      fun <T> myBetterFlowChannel(builder: CoroutineScope.(Channel<T>) -> Unit) = flow {
        coroutineScope {
          val output = Channel<T>()
          builder(output)
          emitAll(output)
        }
      }

      // And now we can use it!
      fun myBetterFlowChannel() = myBetterFlowChannel { channel ->
        launch {
          delay(100)
          channel.send(Flows.chrisEvans)
        }
        launch {
          delay(100)
          channel.send(Flows.robertDowneyJr)
        }
      }

      myBetterFlowChannel().collect { println(it) }
    }
  }
}
