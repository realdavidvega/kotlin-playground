package models

import arrow.optics.Lens
import arrow.optics.optics

@optics
data class GuitarString(val broken: Boolean = false) {
    companion object {
        fun newStrings(): List<GuitarString> = List(5) { GuitarString() }
        fun oldStrings(): List<GuitarString> = List(5) { GuitarString(true) }
    }
}

@optics
data class Instruments(val instruments: List<Instrument>) {
    companion object
}

@optics
sealed class Instrument {
    abstract val model: String

    @optics
    data class Guitar(
        override val model: String = "Model 100",
        val strings: List<GuitarString> = GuitarString.newStrings()
    ) : Instrument() {
        companion object
    }

    @optics
    data class Drums(override val model: String = "Model 100") : Instrument() {
        companion object
    }

    @optics
    data class Microphone(override val model: String = "Model 100") : Instrument() {
        companion object
    }

    companion object {
        val model: Lens<Instrument, String> = Lens(
            get = {
                when (it) {
                    is Guitar -> Guitar.model.get(it)
                    is Drums -> Drums.model.get(it)
                    is Microphone -> Microphone.model.get(it)
                }
            },
            set = { inst, m ->
                when (inst) {
                    is Guitar -> Guitar.model.set(inst, m)
                    is Drums -> Drums.model.set(inst, m)
                    is Microphone -> Microphone.model.set(inst, m)
                }
            }
        )
    }
}

inline val <S> Lens<S, Instrument>.model: Lens<S, String>
    inline get() =
        this + Instrument.model

@optics
data class BandMember(val id: String, val name: String, val instrument: Instrument = Instrument.Drums("Model 101")) {
    companion object
}

@optics
data class BandMembers(val guitarMember: BandMember, val micMember: BandMember, val drumsMember: BandMember) {
    companion object
}

@optics
data class Band(val name: String, val style: String, val members: BandMembers) {
    companion object
}

@optics
data class Poster(val style: String) {
    companion object
}

@optics
data class Stage(val style: String) {
    companion object
}

@optics
data class Concert(val musicStyle: String, val poster: Poster, val stage: Stage) {
    companion object
}

typealias VenueId = String

data class Venue(val id: VenueId, val address: String, val name: String, val capacity: Long)

data class Event(val date: String, val venue: Venue)

data class Discography(val albums: List<Album>)

data class Album(val name: String, val songs: List<String>)
