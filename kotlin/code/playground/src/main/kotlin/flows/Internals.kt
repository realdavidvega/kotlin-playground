package flows

import flows.Flows.benAffleck
import flows.Flows.ezraMiller
import flows.Flows.galGadot
import flows.Flows.henryCavill
import flows.Flows.jasonMomoa
import flows.Flows.rayFisher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// 7. Flows - Flows Appendix

object Internals {

  // This journey let us understand that there is no magic behind flows

  // Let's rebuild the Flow type and the flow builder from scratch
  // We want to implement a function that prints the actors playing in the Justice League movie
  val myFlow: suspend () -> Unit = {
    println(henryCavill)
    println(galGadot)
    println(ezraMiller)
    println(rayFisher)
    println(benAffleck)
    println(jasonMomoa)
  }

  // myFlow()

  // We made the lambda as a suspending function because we want to have the possibility to use
  // nonblocking functions. Now, we want to extend our flow function to print the emitted values
  // and consume them differently. We’ll consume them using a lambda passed to the flow function
  val myBetterFlow: suspend ((Flows.Actor) -> Unit) -> Unit = { emit: (Flows.Actor) -> Unit ->
    emit(henryCavill)
    emit(galGadot)
    emit(ezraMiller)
    emit(rayFisher)
    emit(benAffleck)
    emit(jasonMomoa)
  }

  // myBetterFlow { println(it) }

  // We called emit the input lambda to apply to each emitted value of the flow.
  // To clarify, we also specified the emit lambda type. However, the Kotlin compiler can infer
  // the type of the emit lambda, so we can omit it in the next iteration.

  // We introduced the emit lambda, which represents the consumer logic of the values created
  // by the flow. So, emitting a new value equals applying consuming logic to it.
  // This concept is at the core of the Flow type. The following steps will only make up the
  // code to avoid passing lambdas around

  // We made the FlowCollector interface a functional interface (or SAM, Single Abstract Method)
  // to let the compiler adapt the lambda to the interface
  fun interface MyFlowCollector {
    suspend fun emit(value: Flows.Actor)
  }

  val myEvenBetterFlow: suspend (MyFlowCollector) -> Unit = {
    it.emit(henryCavill)
    it.emit(galGadot)
    it.emit(ezraMiller)
    it.emit(rayFisher)
    it.emit(benAffleck)
    it.emit(jasonMomoa)
  }

  // Possible because of the SAM interface
  // myEvenBetterFlow { println(it) }

  // Since we don’t like to call the emit function on the 'it' reference, we can change the
  // definition of the flow function again. We aim to create a smoother DSL, letting us call
  // the emit function directly. Then, we must make the FlowCollector instance available as
  // the 'this' reference inside the lambda. Using the FlowCollector interface as the receiver
  // of the lambda does the trick
  val myGreaterFlow: suspend MyFlowCollector.() -> Unit = {
    emit(henryCavill)
    emit(galGadot)
    emit(ezraMiller)
    emit(rayFisher)
    emit(benAffleck)
    emit(jasonMomoa)
  }

  // myGreaterFlow { println(it) }

  // Now, we want to avoid passing a function to use our flow.
  // So, it’s time to lift the flow function to a proper type.
  interface MyFlow {
    suspend fun collect(collector: MyFlowCollector)
  }

  val builder: suspend MyFlowCollector.() -> Unit = {
    emit(henryCavill)
    emit(galGadot)
    emit(ezraMiller)
    emit(rayFisher)
    emit(benAffleck)
    emit(jasonMomoa)
  }

  val myNiceFlow: MyFlow =
    object : MyFlow {
      override suspend fun collect(collector: MyFlowCollector) {
        builder(collector)
      }
    }

  // Here, we use the fact that calling a function or lambda with a receiver is equal to passing
  // the receiver as the function’s first argument. Last but not least, we miss the original
  // flow builder of the Kotlin Coroutines library
  fun myNiceFlow(builder: suspend MyFlowCollector.() -> Unit): MyFlow =
    object : MyFlow {
      override suspend fun collect(collector: MyFlowCollector) {
        builder(collector)
      }
    }

  // We want to define flows on every type, not only on the Actor type. So, we need to add a bit of
  // generic magic powder to the code we defined so far

  fun interface MyFinalFlowCollector<T> {
    suspend fun emit(value: T)
  }

  interface MyFinalFlow<T> {
    suspend fun collect(collector: MyFinalFlowCollector<T>)
  }

  fun <T> myFinalFlow(builder: suspend MyFinalFlowCollector<T>.() -> Unit): MyFinalFlow<T> =
    object : MyFinalFlow<T> {
      override suspend fun collect(collector: MyFinalFlowCollector<T>) {
        builder(collector)
      }
    }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val myFinalFlow =
        myFinalFlow {
            emit(1)
            delay(200)
            emit(2)
            delay(200)
            emit(3)
          }
          .collect { println(it) }
    }
  }
}
