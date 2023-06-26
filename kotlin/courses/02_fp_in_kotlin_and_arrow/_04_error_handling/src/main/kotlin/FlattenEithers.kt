import SpeakerErrors.SpeakerNotFound
import arrow.core.Either
import arrow.core.traverse
import models.Band
import models.BandMember
import models.BandMembers
import models.Instrument

// Error handling and data validation
// Flattening Either operations

// Sometimes we have a list of elements to perform an Either operation on, but iterating over the list performing all
// those operations would return a List<Either<A, B>> which is hard to deal with, since we would need to go element
// by element checking whether it's a Left or a Right.

// Arrow provides traverseEither to solve this concern

sealed class SpeakerErrors {
    object SpeakerNotFound : SpeakerErrors()
}

data class Speaker(val id: String, val name: String)

fun loadSpeakers(): List<Speaker> =
    listOf(
        Speaker("id1", "Simon"),
        Speaker("id2", "Jorge"),
        Speaker("id3", "Raul")
    )

fun findSpeaker(id: String): Either<SpeakerNotFound, Speaker> =
    Either.fromNullable(loadSpeakers().find { it.id == id }).mapLeft { SpeakerNotFound }

val ids = listOf("id1", "id2", "id3")
var speakers: List<Either<SpeakerNotFound, Speaker>> =
    ids.map { findSpeaker(it) } // 🚨

// Doesn't take the short-circuiting behavior into account. It runs for all ids regardless of the result of each call.

// Let's fix it with traverse:
val flattenSpeakers: Either<SpeakerErrors, List<Speaker>> =
    ids.traverse { findSpeaker(it) }
// Flattened, and short-circuits!

// Note how the result effectively swaps the data types. What it was List<Either<A, B>> before is
// Either<A, List<B>> now, which is much easier to handle.

/////////////

// Some bands

val raul = BandMember(id = "1", name = "Raúl", instrument = Instrument.Guitar())
val benjy = BandMember(id = "2", name = "Benjy Montoya", instrument = Instrument.Microphone())
val simon = BandMember(id = "3", name = "Simon", instrument = Instrument.Drums())
val bandMembers = BandMembers(raul, benjy, simon)
val band1 = Band("The Free Monads", "rock", bandMembers)

val john = BandMember(id = "4", name = "John", instrument = Instrument.Guitar())
val mary = BandMember(id = "5", name = "Mary", instrument = Instrument.Microphone())
val ada = BandMember(id = "6", name = "Ada", instrument = Instrument.Drums())
val bandMembers2 = BandMembers(john, mary, ada)
val band2 = Band("Some band 2", "rap", bandMembers2)

val eve = BandMember(id = "7", name = "Eve", instrument = Instrument.Guitar())
val greta = BandMember(id = "8", name = "Greta", instrument = Instrument.Microphone())
val jack = BandMember(id = "9", name = "Jack", instrument = Instrument.Drums())
val bandMembers3 = BandMembers(eve, greta, jack)
val band3 = Band("Some band 3", "hard metal", bandMembers3)

val bands = listOf(band1, band2, band3)

/////////////

fun loadBand(name: String): Either<Unit, Band> =
    Either.fromNullable(bands.find { it.name == name })

fun loadBandsByNameInefficient(names: List<String>): List<Either<Unit, Band>> =
    names.map { loadBand(it) }

// loadBandsByNameEfficient loads all bands by their names and flattens the result into a single Either
// for easier processing later.
fun loadBandsByNameEfficient(names: List<String>): Either<Unit, List<Band>> =
    // this will flatter and return it an Either of an error or a List of bands
    names.traverse { loadBand(it) }

// if we would have on loadBand a mapLeft mapped so something else instead of Unit, like a BandNotFound,
// then that would be the error type on the Either

// processResult, uses that improvement, so processes the result of the previous by folding on it and:
// Return "Error!" if it's a Left.
// Return the band names separated by commas (without spaces) when it's a Right.
fun processResult(names: List<String>): String =
    loadBandsByNameEfficient(names).fold(
        ifLeft = { "Error!" },
        ifRight = { bands -> bands.joinToString(",") { it.name } }
    )

fun main() {
    val names = listOf("The Free Monads", "Some band 2")
    val bandNamesFound = processResult(names)
    println(bandNamesFound)
}