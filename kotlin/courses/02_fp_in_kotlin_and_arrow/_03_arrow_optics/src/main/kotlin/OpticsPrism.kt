import arrow.core.identity
import arrow.core.left
import arrow.core.right
import arrow.optics.Prism
import models.Instrument
import models.microphone

// A Prism can see into a structure and optionally find its focus.

// Often used for structures that have a relationship only under a certain condition, e.g: a Kotlin sealed class.
// E.g: You have a sealed class, and you want it to act over just one of the types of the hierarchy.

// Note how it uses Either.Right to indicate the successful focus, and Either.Left for the failed focus.
// (With Either<L, R> we always reflect errors on the Left side by convention).

sealed class NetworkResult {
    data class Success(val content: String): NetworkResult()
    object Failure: NetworkResult()
}

val networkSuccessPrism: Prism<NetworkResult, NetworkResult.Success> =
    Prism(
        getOrModify = { networkResult ->
            when(networkResult) {
                is NetworkResult.Success -> networkResult.right()
                else -> networkResult.left()
            }
        },
        reverseGet = { networkResult -> networkResult } //::identity
    )

// Prisms are generated for all the properties of an @optics annotated class.

// In the case of an annotated sealed class, Prism is generated for each one of its elements and can be accessed
// by Companion.childlowercasename as if they were standard properties. E.g: NetworkResult.success.

// You can get the auto generated prism the same way you did for the lenses.
// Starting on the companion object of the class.

// micPrism() function returns a Prism that is able to focus on an Instrument only when it is a Microphone.
fun micPrism(): Prism<Instrument, Instrument.Microphone> =
    // we import microphone which is a auto-generated Prism thanks to Optics
    Instrument.microphone

// Instrument.updateMicModel(newModel: String) function uses the obtained prism to update the microphone model
// to the given one.
fun Instrument.updateMicModel(newModel: String): Instrument =
    micPrism().modify(this) { microphone ->
        microphone.copy(model = newModel)
    }

// also works
// micPrism().model.modify(this) { newModel }

// Take a look to the official Prism docs for deeper details: https://arrow-kt.io/docs/optics/prism/
