import Errors.ConcertsNotFound
import Errors.InvalidBands
import Errors.BandNotFound
import arrow.core.*
import arrow.core.continuations.either
import models.Band
import models.Concert
import models.Poster
import models.Stage

// Error handling and data validation
// Failing fast
// Either.flatMap allows us to chain operations that return Either. flatMap defines sequential,
// or in other words, dependent computations.

sealed class Errors {
    object BandNotFound : Errors()
    object ConcertsNotFound : Errors()
    object InvalidBands : Errors()
}

class MusicFestivalService {
    fun loadBands(): Either<BandNotFound, List<Band>> =
            Errors.BandNotFound.left()

    fun loadBandConcerts(bandNames: List<String>): Either<ConcertsNotFound, List<Concert>> =
        listOf(Concert("rock", Poster("rock"), Stage("rock"))).right()
}

private fun List<Band>.validate(): Either<InvalidBands, List<Band>> =
    if (isEmpty()) {
        InvalidBands.left()
    } else {
        this.right()
    }

// Loads the bands from the service first.
// Validates the loaded bands using validate() method for it.
// Load concerts for all the valid bands using their names.
fun MusicFestivalService.loadConcertsForValidBands(): Either<Errors, List<Concert>> =
    either.eager {
        val bands = loadBands().bind()
        val notEmptyBands = bands.validate().bind()
        loadBandConcerts(notEmptyBands.map { it.name }).bind()
    }

// equivalent to:
fun MusicFestivalService.loadConcertsForValidBandsFlat(): Either<Errors, List<Concert>> =
    loadBands().flatMap { it.validate() }
        .flatMap { nonEmptyBands -> loadBandConcerts(nonEmptyBands.map { it.name })}


// this does exactly this:
//when (this) {
//    is Either.Left -> shift(value)
//    is Either.Right -> value
//}
// Each EffectScope and EagerEffectScope expose shift, where you can short-circuit to R in this case any error defined
// on Errors sealed class

// app.loadConcertsSafely calls app.loadConcertsForValidBands and fallbacks to an emptyList()
// in case there was any error.
fun MusicFestivalService.loadConcertsSafely(): Either<Errors, List<Concert>> =
    // recover with direct value or simple value
    loadConcertsForValidBands().handleError { emptyList() }

fun MusicFestivalService.loadConcertsSafelyAnother(): Either<Errors, List<Concert>> =
    loadConcertsForValidBands().handleErrorWith { TODO() } // we could add here anotherOperationWithEither()

// So in case there is a short-circuit, we handle the error of Either and return an empty list

// Use either.eager {} block for the function as we've seen on slides. Use bind to resolve either calls on.

// the inside and short-circuit when there's an error.

fun main() {
    val service = MusicFestivalService()
    val concerts = service.loadConcertsSafely()
    println(concerts)

    val concerts2 = service.loadConcertsForValidBandsFlat().handleError { emptyList() }
    println(concerts2)
}
