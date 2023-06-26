import models.*

// A pure function should only use its input arguments to provide a result.
// A pure function does not access or alter the outside world when invoked and perform any changes in the program state.
// A pure function when invoked will not perform I/O, logging, network calls or anything that will make it
// non-deterministic producing different results each time is invoked.
// If a pure function requires to perform a side effect, we'll need to get the effect under control.
fun conformBand(
    name: String,
    style: String,
    guitar: BandMember,
    mic: BandMember,
    drums: BandMember
): Band =
    Band(
        name,
        style,
        BandMembers(guitar, mic, drums)
    )

// dummy store effect
class MusicDB {
    fun store(band: Band): Unit =
        throw RuntimeException("Store is an effect and makes this function and any callers impure!")
}

// dummy db
val musicDatabase: MusicDB = MusicDB()


// A pure function does not alter the outside world when invoked and performs no observable changes.
// A pure function when invoked will not perform I/O, logging, network calls or anything that will make it
// non-deterministic producing different results each time is invoked.
fun conformOtherBand(
    name: String,
    style: String,
    guitar: BandMember,
    mic: BandMember,
    drums: BandMember
): Band {
    val band = Band(
        name,
        style,
        BandMembers(guitar, mic, drums)
    )
    // this makes this function impure and causes an observable change in the outside world when invoked -> side effect
    musicDatabase.store(band)
    return band
}

// A pure function (or expression) can be replaced by its value everywhere in a program without changing
// the overall program behavior. That is why we say Referential Transparency is based on the substitution model.
// When a function is pure, it always returns the same value for the same inputs.
// Therefore, it's referentially transparent since we could replace it by its value in all the cases where it is
// called for some given inputs.
fun prepareConcert(): Concert {
    val david = BandMember(id = "1", name = "David", instrument = Instrument.Guitar())
    val jose = BandMember(id = "2", name = "Jose", instrument = Instrument.Microphone())
    val alex = BandMember(id = "3", name = "Alex", instrument = Instrument.Drums())

    val poster = Poster(
        Band(
            "some band",
            "rock",
            BandMembers(david, jose, alex)
        ).style
    )
    val stage = Stage(
        Band(
            "some band",
            "rock",
            BandMembers(david, jose, alex)
        ).style
    )
    return Concert(
        Band(
            "some band",
            "rock",
            BandMembers(david, jose, alex)
        ).style,
        Poster(
            Band(
                "some band",
                "rock",
                BandMembers(david, jose, alex)
            ).style),
        Stage(
            Band(
                "some band",
                "rock",
                BandMembers(david, jose, alex)
            ).style
        )
    )
}

fun main() {
    val dani = BandMember(id = "1", name = "Dani", instrument = Instrument.Guitar())
    val jose = BandMember(id = "2", name = "Jose", instrument = Instrument.Microphone())
    val alex = BandMember(id = "3", name = "Alex", instrument = Instrument.Drums())
    val band = conformBand("some band", "rock", dani, jose, alex)
    println("$band")
    
    // not pure function
    val otherBand = conformOtherBand("some band", "rock", dani, jose, alex)
    println("$otherBand")

    // we can replace all those expressions by their resulting value everywhere
    // that is why we say referential transparency is based on the substitution model
    val concert = prepareConcert()
    println(concert)
}
