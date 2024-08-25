package flows

import flows.ErrorHandling.actorRepository
import flows.Flows.avengers
import flows.Flows.benAffleck
import flows.Flows.galGadot
import flows.Flows.henryCavill
import flows.Flows.zackSnyderJusticeLeague
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking

/**
 * (6) Flows - Racing/Merging and Concurrent Transformers
 */
object Racing {

  interface BiographyRepository {
    suspend fun findBio(actor: Flows.Actor): Flow<String>
  }

  interface MovieRepository {
    suspend fun findMovies(actor: Flows.Actor): Flow<String>
  }

  // default implementation we can use
  val biographyRepository: BiographyRepository =
    object : BiographyRepository {
      val biosByActor =
        mapOf(
          henryCavill to
            listOf(
              "1983/05/05",
              "Henry William Dalgliesh Cavill was born on the Bailiwick of Jersey, a British Crown",
              "Man of Steel, Batman v Superman: Dawn of Justice, Justice League",
            ),
          benAffleck to
            listOf(
              "1972/08/15",
              "Benjamin Géza Affleck-Boldt was born on August 15, 1972 in Berkeley, California.",
              "Argo, The Town, Good Will Hunting, Justice League",
            ),
        )

      override suspend fun findBio(actor: Flows.Actor): Flow<String> =
        biosByActor[actor]?.asFlow() ?: emptyFlow()
    }

  // default implementation
  val movieRepository =
    object : MovieRepository {
      val filmsByActor: Map<Flows.Actor, List<String>> =
        mapOf(
          henryCavill to
            listOf("Man of Steel", "Batman v Superman: Dawn of Justice", "Justice League"),
          benAffleck to listOf("Argo", "The Town", "Good Will Hunting", "Justice League"),
          galGadot to listOf("Fast & Furious", "Justice League", "Wonder Woman 1984"),
        )

      override suspend fun findMovies(actor: Flows.Actor): Flow<String> =
        filmsByActor[actor]?.asFlow() ?: emptyFlow()
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // Flows are a very nice data structure that works with concurrency.

      // merge
      // It lets us run two flows concurrently, collecting the results in a single flow as they
      // are produced. Neither of the two streams waits for the other to emit a value
      val zackSnyderJusticeLeague_delayed = zackSnyderJusticeLeague.onEach { delay(250) }
      val avengers_delayed = avengers.onEach { delay(250) }

      // As we may expect, the flow contains an actor from the JLA almost every two actors from the
      // Avengers. Once the Avengers actors are finished, the JLA actors fulfill the rest of the
      // flow. The execution halts when both flows have finished emitting all their values.
      merge(zackSnyderJusticeLeague_delayed, avengers_delayed).collect { println(it) }
      println("-------------------")

      // zip
      // Sometimes, we must work with pairs of emitted values from both flows.
      // Imagine a scenario where we want to retrieve the biography and filmography of an actor.
      // The two pieces of information are retrieved from different services, and we want to get
      // them concurrently and proceed with the execution only when both are available.
      val henryCavillBio = flow {
        delay(1000)
        val biography =
          """|
             |Henry William Dalgliesh Cavill was born on the Bailiwick of Jersey, a British Crown dependency 
             |in the Channel Islands. His mother, Marianne (Dalgliesh), a housewife, was also born on Jersey, 
             |and is of Irish, Scottish and English ancestry...
             |"""
            .trimMargin()
        emit(biography)
      }

      val henryCavillMovies = flow {
        delay(2000)
        val movies = listOf("Man of Steel", "Batman v Superman: Dawn of Justice", "Justice League")
        emit(movies)
      }

      // Be aware that the zip function requires pairs of values.
      // So, the resulting flow stops when the shortest of the two flows stops emitting values
      henryCavillBio
        .zip(henryCavillMovies) { bio, movies -> bio to movies }
        .collect { (bio, movies) ->
          println(
            """|
               |Henry Cavill
               |------------
               |BIOGRAPHY:
               |$bio
               |MOVIES:
               |${movies.joinToString("\n")}
               |"""
              .trimMargin()
          )
        }

      println("-------------------")

      // If we zip a flow with the empty flow, the resulting flow will also be empty.
      // The following zipped flow does not emit any value
      henryCavillBio
        .zip(emptyFlow<List<String>>()) { bio, movies -> bio to movies }
        .collect { (bio, movies) ->
          println(
            """|
               |Henry Cavill
               |------------
               |BIOGRAPHY:
               |$bio
               |MOVIES:
               |${movies.joinToString("\n")}
               |"""
              .trimMargin()
          )
        }

      // flatMapConcat
      // Processes the emitted values of the first flow and, for each, the associated emitted
      // values of the second flow. In other words, it waits for the second flow to emit all
      // its values before processing the next value of the first flow
      actorRepository
        .findJLAActors()
        .retry(2)
        .filter { it == benAffleck || it == henryCavill }
        .flatMapConcat { actor -> biographyRepository.findBio(actor) }
        .collect { println(it) }

      println("-------------------")

      // flatMapMerge
      // Adds an input parameter: the number of concurrent operations we want to execute.
      // The default value of concurrency is 16.
      // Despite the concurrency degree, the rest of the definition is usual for a flatMap function.
      // Is handy when dealing with I/O operations on a collection of information.
      // We can set the concurrency level to the number of available processors to maximize the
      // program’s performance or even to fine-tune the maximum level of resources we want to use
      actorRepository
        .findJLAActors()
        .filter { it == benAffleck || it == henryCavill || it == galGadot }
        .flatMapMerge { actor -> movieRepository.findMovies(actor).onEach { delay(1000) } }
        .collect { println(it) }
    }
  }
}
