package flows

import flows.Flows.Actor
import flows.Flows.benAffleck
import flows.Flows.ezraMiller
import flows.Flows.galGadot
import flows.Flows.henryCavill
import flows.Flows.jasonMomoa
import flows.Flows.rayFisher
import flows.Flows.zackSnyderJusticeLeague
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking

/** (4) Flows - Transformations */
object Transformations {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // Flows are very similar to collections regarding the API available for transforming them.
      // We can map, filter, and reduce them.

      // map
      val lastNameOfJLActors: Flow<Flows.LastName> = zackSnyderJusticeLeague.map { it.lastName }

      lastNameOfJLActors.collect { println(it) }
      println("-------------------")

      // filter
      val lastNameOfJLActors5CharsLong: Flow<Flows.LastName> =
        lastNameOfJLActors.filter { it.value.length == 5 }

      lastNameOfJLActors5CharsLong.collect { println(it) }
      println("-------------------")

      // map not null
      val lastNameOfJLActors5CharsLong_v2: Flow<Flows.LastName> =
        zackSnyderJusticeLeague.mapNotNull {
          if (it.lastName.value.length == 5) {
            it.lastName
          } else {
            null
          }
        }

      lastNameOfJLActors5CharsLong_v2.collect { println(it) }
      println("-------------------")

      // fold
      // The fold function is used to reduce the values of a flow to a single
      // value. It’s a final operation, like the collect function, which suspends the current
      // coroutine until the flow ends to emit values. It requires an initial value used to
      // accumulate the final result
      val numberOfJlaActors: Int =
        zackSnyderJusticeLeague.fold(0) { currentNumOfActors, actor -> currentNumOfActors + 1 }

      println(numberOfJlaActors)
      println("-------------------")

      // count
      // A dedicated function called count counts the number of elements of a finite flow.
      // It’s a terminal operation that returns the number of elements the flow emits
      val numberOfJlaActors_v2: Int = zackSnyderJusticeLeague.count()

      println(numberOfJlaActors_v2)
      println("-------------------")

      // Fold and the count functions don’t work well with infinite flows.

      // scan
      // The library gives us a dedicated function for infinite flows, the scan function.
      // It works like a fold, accumulating the emitted values. However, it emits the result of
      // the partial accumulation of each step. Unlike the fold function, the scan function is
      // not a terminal operation

      // Infinite flow, does not emit values during creation
      val infiniteJLFlowActors: Flow<Actor> = flow {
        // It won't block the application, they run in their separate coroutines
        while (true) {
          emit(henryCavill)
          emit(galGadot)
          emit(ezraMiller)
          emit(rayFisher)
          emit(benAffleck)
          emit(jasonMomoa)
          throw RuntimeException("Oooops")
        }
      }

      infiniteJLFlowActors
        .onEach { delay(1000) }
        .map { "${it.firstName.value.uppercase() } ${it.lastName.value.uppercase()}" }
        .scan(0) { currentNumOfActors, actor -> currentNumOfActors + 1 }
        .catch { println(it) }
        .collect { println(it) }

      println("-------------------")

      // take
      // When dealing with infinite flows, we can always get the flow’s first n elements and
      // then stop the collection
      infiniteJLFlowActors.take(3).collect { println(it) }
      println("-------------------")

      // drop
      // The drop function makes the opposite operation. It skips the flow’s first n elements and
      // then emits the remaining ones.
      infiniteJLFlowActors.drop(3)

      // Dropping from the head of a flow the first n elements does not reduce
      // the cardinality of an infinite flow. The new flow will be infinite as well.
    }
  }
}
