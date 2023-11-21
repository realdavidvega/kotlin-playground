import models.Band
import models.Instrument

// Immutability helps to avoid unintended data modification.
// It's recommended to use immutable data structures as our program's data when you aim for determinism.

// Kotlin provides val modifier for read only properties. You can represent immutable state using data classes
// with read only properties.

// The issue with this is the resulting cumbersome syntax when you need to work with nested immutable data structures.

// Taking the Band model we have in models, implement updateDrums(newModel: String) function so, it returns a
// new Band with the Drums model updated to the new value.
fun Band.updateDrums(newModel: String): Band =
    // copy the Band
    this.copy(
        // copy and modify the members
        members = this.members.copy(
            // copy and modify the drums member
            drumsMember = this.members.drumsMember.copy(
                // copy and modify the instrument
                // because it's a Drums instrument, and Instrument itself it's a sealed class with an abstract
                // field model, we have to match the types (Instrument.Drums and others)
                instrument = when (val instrument = this.members.drumsMember.instrument) {
                    is Instrument.Drums -> instrument.copy(model = newModel)
                    else -> instrument
                }
            )
        )
    )

// Kotlin copy function docs: https://kotlinlang.org/docs/data-classes.html#copying
