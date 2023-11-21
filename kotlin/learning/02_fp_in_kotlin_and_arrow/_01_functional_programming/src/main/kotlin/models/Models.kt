package models

data class GuitarString(val broken: Boolean = false) {
    companion object {
        fun newStrings(): List<GuitarString> = List(5) { GuitarString() }
    }
}

sealed class Instrument {
    abstract val model: String

    data class Guitar(
        override val model: String = "Model 100",
        val strings: List<GuitarString> = GuitarString.newStrings()
    ) : Instrument() {
        companion object
    }

    data class Drums(override val model: String = "Model 100") : Instrument() {
        companion object
    }

    data class Microphone(override val model: String = "Model 100") : Instrument() {
        companion object
    }
}

data class BandMember(val id: String, val name: String, val instrument: Instrument = Instrument.Drums("Model 101")) {
    companion object
}

data class BandMembers(val guitarMember: BandMember, val micMember: BandMember, val drumsMember: BandMember) {
    companion object
}

data class Band(val name: String, val style: String, val members: BandMembers) {
    companion object
}

data class Poster(val style: String) {
    companion object
}

data class Stage(val style: String) {
    companion object
}

data class Concert(val musicStyle: String, val poster: Poster, val stage: Stage) {
    companion object
}
