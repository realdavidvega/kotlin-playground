import arrow.optics.Lens
import models.*

// Working with Kotlin immutable data classes can be cumbersome. Optics solve this issue.
// Lens is an optic to read or modify a deep property within an immutable structure.
// It ultimately is a derived pair of getter and setter.

val member = BandMember("1", "Simon", Instrument.Guitar())

val memberInstrumentLens: Lens<BandMember, Instrument> = Lens(
    get = { member -> member.instrument },
    set = { member, value -> member.copy(instrument = value) }
)

val res = memberInstrumentLens.modify(member) { Instrument.Drums() }

//The code above is all generated, we copied it here for didactic purposes.

// You can obtain a Lens over the companion object of the class annotated with @optics,
// and remember Lenses compose together:
// Band.members.guitarMember.instrument.guitar.strings

// Band.updateDrums(newModel: String) function uses a lens to modify the drums model.
fun Band.updateDrumsLens(newModel: String): Band {
    val drumsLens: Lens<Band, String> = Band.members.drumsMember.instrument.model
    return drumsLens.modify(this) { newModel }

    // we could also use a Prism for this, and we would achieve the same result (with a different path):
    // return Band.members.drumsMember.instrument.drums.model.modify(this) { newModel }
}

// Arrow lens docs: https://arrow-kt.io/docs/optics/lens/
