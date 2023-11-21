import arrow.core.*
import arrow.optics.Prism
import models.Instrument
import models.microphone

// A Prism can see into a structure and optionally find its focus. They’re mostly used for structures that
// have a relationship only under a certain condition, such as a hierarchy of sealed classes where the value can
// be only of one type at a time in the hierarchy.

// Note how it uses Either.Right to indicate the successful focus, and Either.Left for the failed focus.
// With Either<L, R> we always reflect errors on the Left side by convention.
sealed class AnotherNetworkResult {
    data class Success(val content: String): AnotherNetworkResult()
    object Failure: AnotherNetworkResult()
}

val anotherNetworkSuccessPrism: Prism<AnotherNetworkResult, AnotherNetworkResult.Success> =
    Prism(
        getOrModify = { anotherNetworkResult ->
            when(anotherNetworkResult) {
                is AnotherNetworkResult.Success -> anotherNetworkResult.right()
                else -> anotherNetworkResult.left()
            }
        },
        reverseGet = { anotherNetworkResult -> anotherNetworkResult } //::identity or { it }
    )

// Instrument.microphonePrism() function to return a Prism that is able to focus on an Instrument
// only when it is a Microphone. However, in this case it's a manual prism.
fun anotherMicPrism(): Prism<Instrument, Instrument.Microphone> =
    // we import microphone which is an auto-generated Prism thanks to Optics
    Prism(
        getOrModify = { instrument ->
            when (instrument) {
                // if it's an instance of microphone, we return it
                is Instrument.Microphone -> instrument.right()

                // if not, we return the instrument
                else -> instrument.left()
            }

        },
        reverseGet = ::identity
    )

// we can also implement it with getOption, and it's the same
fun optionMicPrism(): Prism<Instrument, Instrument.Microphone> =
    // we import microphone which is an auto-generated Prism thanks to Optics
    Prism(
        getOption = { instrument ->
            when (instrument) {
                // when we are able to focus
                is Instrument.Microphone -> instrument.some()

                // when we are not able to focus
                else -> None
            }

        },
        reverseGet = { it }
    )
