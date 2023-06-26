import arrow.core.Either
import models.Band
import models.BandMember
import models.BandMembers
import models.Instrument

// Either<L, R> represents an exclusive disjunction between two typed values. A value in Either can be either
// of type R which represents the happy path, or it can have type L, which is the alternative path.

// It is often used to model and make explicit the duality between error and success in our domain.
// It is a sealed class with two possible implementations: Left and Right.
// You have examples of some error handling strategies in the official docs.

object BandNotFound

class BandCache {
    private val bands = listOf(
        Band("Benjy Montoya & The Free Monads", "rock",
            BandMembers(
                BandMember(id = "2", name = "Raúl", instrument = Instrument.Guitar()),
                BandMember(id = "3", name = "Benjy Montoya", instrument = Instrument.Microphone()),
                BandMember(id = "1", name = "Simon", instrument = Instrument.Drums()))
        )
    )

    // findBand(name: String) function find a Band by name in the local cache and return
    // Either<BandNotFound, Band>. Let's try to use Either.fromNullable.
    fun findBand(name: String): Either<BandNotFound, Band> =
        Either.fromNullable(bands.find { it.name == name }).mapLeft { BandNotFound }
}

private fun Band.memberNames(): String =
    "${members.guitarMember.name}, ${members.micMember.name}, ${members.drumsMember.name}"

// displayBandMembers() calls the former and then processes the result of that call by returning:
// A "Band not found!" message when the band was not found.
// The band member names when the band was found. For this, it can use the provided memberNames() extension function.
fun displayBandMembers(cache: BandCache, name: String): String =
    cache.findBand(name).fold(
        ifLeft = { "Band not found!" },
        ifRight = { band -> band.memberNames() }
    )

// As you know find returns a nullable value, we know how to lift that into Either.

// Either provides fold as a mechanism to ensure we treat both sides.

fun main() {
    val cache = BandCache()
    val members = displayBandMembers(cache, "Benjy Montoya & The Free Monads")
    println(members)

    val members2 = displayBandMembers(cache, "AC/DC")
    println(members2)
}
