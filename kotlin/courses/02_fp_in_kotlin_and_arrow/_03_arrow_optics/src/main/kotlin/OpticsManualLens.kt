import arrow.optics.Lens
import models.BandMember
import models.Instrument

// Lens represents a functional reference for a getter and setter that can focus on a property access its
// value and modify it.

val memberInstrumentManualLens: Lens<BandMember, Instrument> = Lens(
    get = { member -> member.instrument },
    set = { member, value -> member.copy(instrument = value) }
)

val someMember = BandMember("1", "Simon", Instrument.Guitar())
val someRes = memberInstrumentLens.modify(member) { Instrument.Drums() }

// Manual Lens instead of relying on the generated ones.
// The Lens must be able to read and modify the Instrument.Guitar model property.
fun createInstrumentLens(): Lens<Instrument.Guitar, String> = Lens(
    get = { guitar -> guitar.model },
    set = { guitar, model -> guitar.copy(model = model) }
)

// Arrow Lens docs: https://arrow-kt.io/docs/next/optics/lens/
