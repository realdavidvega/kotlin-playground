package flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import flow.Flows.andrewGarfield
import flow.Flows.benAffleck
import flow.Flows.chrisEvans
import flow.Flows.chrisHemsworth
import flow.Flows.ezraMiller
import flow.Flows.galGadot
import flow.Flows.henryCavill
import flow.Flows.jasonMomoa
import flow.Flows.jeremyRenner
import flow.Flows.markRuffalo
import flow.Flows.rayFisher
import flow.Flows.robertDowneyJr
import flow.Flows.scarlettJohansson
import flow.Flows.tobeyMaguire
import flow.Flows.tomHolland

// 8. Flows and Coroutines

object Flows {
  @JvmInline value class Id(val id: Int)

  @JvmInline value class FirstName(val firstName: String)

  @JvmInline value class LastName(val lastName: String)

  data class Actor(val id: Id, val firstName: FirstName, val lastName: LastName)

  // Zack Snyder's Justice League
  val henryCavill = Actor(Id(1), FirstName("Henry"), LastName("Cavill"))
  val galGadot: Actor = Actor(Id(1), FirstName("Gal"), LastName("Gadot"))
  val ezraMiller: Actor = Actor(Id(2), FirstName("Ezra"), LastName("Miller"))
  val rayFisher: Actor = Actor(Id(3), FirstName("Ray"), LastName("Fisher"))
  val benAffleck: Actor = Actor(Id(4), FirstName("Ben"), LastName("Affleck"))
  val jasonMomoa: Actor = Actor(Id(5), FirstName("Jason"), LastName("Momoa"))

  // The Avengers
  val robertDowneyJr: Actor = Actor(Id(6), FirstName("Robert"), LastName("Downey Jr."))
  val chrisEvans: Actor = Actor(Id(7), FirstName("Chris"), LastName("Evans"))
  val markRuffalo: Actor = Actor(Id(8), FirstName("Mark"), LastName("Ruffalo"))
  val chrisHemsworth: Actor = Actor(Id(9), FirstName("Chris"), LastName("Hemsworth"))
  val scarlettJohansson: Actor = Actor(Id(10), FirstName("Scarlett"), LastName("Johansson"))
  val jeremyRenner: Actor = Actor(Id(11), FirstName("Jeremy"), LastName("Renner"))

  // Spider-Man
  val tomHolland: Actor = Actor(Id(12), FirstName("Tom"), LastName("Holland"))
  val tobeyMaguire: Actor = Actor(Id(13), FirstName("Tobey"), LastName("Maguire"))
  val andrewGarfield: Actor = Actor(Id(14), FirstName("Andrew"), LastName("Garfield"))
}

suspend fun main() {
  // A Flow<T> is a reactive data structure that emits a sequence of type T values

  // We can create a flow from a finite list of values using the flowOf function
  // Actually is not a data structure, but a concurrent data structure
  val zackSnyderJusticeLeague: Flow<Flows.Actor> =
    flowOf(henryCavill, galGadot, ezraMiller, rayFisher, benAffleck, jasonMomoa)

  // Or create a flow from a list, a set, and so on using the asFlow extension function
  // Again, we are emitting values
  val avengers: Flow<Flows.Actor> =
    listOf(
      robertDowneyJr,
      chrisEvans,
      markRuffalo,
      chrisHemsworth,
      scarlettJohansson,
      jeremyRenner,
    )
      .asFlow()

  // If we have a function that returns a value, we can create a flow from it using the
  // asFlow function as well
  val theMostRecentSpiderManFun: () -> Flows.Actor = { tomHolland }

  // Emits the value returned by the function
  val theMostRecentSpiderMan: Flow<Flows.Actor> = theMostRecentSpiderManFun.asFlow()

  // Flows are consumed, and will emit values at the time

  // The flow function is the most general way to create a flow, and emit the values
  // All the above Flow constructors are in fact built with the flow function
  val spiderMen: Flow<Flows.Actor> = flow {
    emit(tobeyMaguire)
    emit(andrewGarfield)
    emit(tomHolland)
  }

  // The lambda passed as an input parameter to the flow function defines an instance
  // of a functional interface called FlowCollector as its receiver. And gives some
  // functions like emit

  // It’s easy to define the previous factory methods in terms of the flow function
  fun <T> flowOf(vararg values: T): Flow<T> = flow {
    for (value in values) {
      emit(value)
    }
  }

  // Needs to be called from a coroutine as it's a suspend function
  spiderMen.collect { println(it) }

  // The collect method is a suspending function since it has to wait and suspend for consuming
  // the emitted values without blocking a thread.
  suspend fun <T> Flow<T>.collect(collector: FlowCollector<T>): Unit = TODO()

  // These emit are not necessarily immediate or synchronous, they can take a while
  val slowerSpiderMen: Flow<Flows.Actor> = flow {
    emit(tobeyMaguire)
    delay(1000)
    emit(andrewGarfield)
    delay(1000)
    emit(tomHolland)
  }

  // Since it’s a reactive data structure, the values in a flow are only computed once requested
  // A Flow<T> is just a definition of calculating the values, not the values themselves,
  // which is a fundamental difference with collections, sequences, and iterables

  // We can listen when the item is created or when the flow start emitting things
  // And do some stuff!
  slowerSpiderMen
    .onStart { println("started") }
    .onEach { delay(100) }
    .onCompletion { println("flow finished") }
    .collect { println(it) }

  // The onStart function lets us add operations to be executed when the flow is started.
  fun <T> Flow<T>.onStart(
    action: suspend FlowCollector<T>.() -> Unit
  ): Flow<T> = TODO()

  // Infinite flow, does not emit values during creation
  val infiniteJLFlowActors: Flow<Flows.Actor> = flow {
    // It won't block the application, they run in their separate coroutines
    while (true) {
      emit(henryCavill)
      emit(galGadot)
      emit(ezraMiller)
      emit(rayFisher)
      emit(benAffleck)
      emit(jasonMomoa)
    }
  }

}
