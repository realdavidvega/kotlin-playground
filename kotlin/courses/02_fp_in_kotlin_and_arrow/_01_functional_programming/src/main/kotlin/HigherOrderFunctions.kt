import models.Band
import models.BandMember
import models.Instrument

// Functions that get other functions as arguments, or functions that return other functions.
// This is only possible because functions are treated as first class citizens in the Kotlin language.
// We can store a function in a value and pass it around as an argument and defer its invocation until needed.
fun Band.upgradeAllInstruments(updater: (Instrument) -> Instrument): Band {
    val members = this.members
    val guitarMember = members.guitarMember
    val micMember = members.micMember
    val drumsMember = members.drumsMember

    return this.copy(
        members = members.copy(
            guitarMember = guitarMember.copy(
                instrument = updater(guitarMember.instrument)
            ),
            micMember = micMember.copy(
                instrument = updater(micMember.instrument)
            ),
            drumsMember = drumsMember.copy(
                instrument = updater(drumsMember.instrument)
            )
        )
    )
}

// it calls upgradeAllInstruments by passing it the upgrade function to it.
// returns a band with upgraded instruments, does not modify the original one, so no side effect.
fun Band.upgradeInstruments(newModel: String): Band =
    upgradeAllInstruments { it.upgrade(newModel) }

// it returns a copy of the instrument with its model updated.
fun Instrument.upgrade(newModel: String): Instrument =
    when (this) {
        is Instrument.Guitar -> this.copy(model = newModel)
        is Instrument.Drums -> this.copy(model = newModel)
        is Instrument.Microphone -> this.copy(model = newModel)
    }

fun main() {
    val javier = BandMember(id = "1", name = "Javier", instrument = Instrument.Guitar())
    val david = BandMember(id = "2", name = "David", instrument = Instrument.Microphone())
    val alex = BandMember(id = "3", name = "Alex", instrument = Instrument.Drums())
    val band = conformBand("some band", "rock", javier, david, alex)

    // we upgrade all models of the instruments of the band
    val upgradedBand = band.upgradeInstruments("Model 200")
    println(upgradedBand.members.guitarMember.instrument.model)
    println(upgradedBand.members.guitarMember.instrument.model)
    println(upgradedBand.members.guitarMember.instrument.model)
}
