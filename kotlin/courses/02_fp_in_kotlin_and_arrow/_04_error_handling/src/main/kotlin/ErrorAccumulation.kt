import GuitarErrors.BrokenStrings
import GuitarErrors.OldModel
import arrow.core.ValidatedNel
import arrow.core.invalidNel
import arrow.core.validNel
import arrow.core.zip
import models.GuitarString
import models.Instrument.Guitar

// Error handling and data validation
// ValidatedNel (Validated<NonEmptyList , A>)
// One of the most common use cases for Validated is to use it in combination with NonEmptyList<E>
// for error accumulation.

sealed class GuitarErrors {
    object BrokenStrings : GuitarErrors()
    object OldModel : GuitarErrors()
}

// Returns a ValidatedNel containing the BrokenStrings error if any of the GuitarStrings is broken.
// Returns a ValidatedNel containing the OldModel error if the model is "Old model".
// Returns a ValidatedNel containing the valid Guitar for any other case.
// For the sake of simplicity, let's assume that for the same Guitar instance validation only one of these errors
// can happen at a time. (No need for zip on this one).
fun Guitar.validate(): ValidatedNel<GuitarErrors, Guitar> =
    when {
        strings.any { it.broken } -> BrokenStrings.invalidNel()
        model == "Old model" -> OldModel.invalidNel()
        else -> this.validNel()
    }

val res1 = Guitar(model = "New model", strings = GuitarString.oldStrings()).validate()
val res2 = Guitar(model = "Old model", strings = GuitarString.newStrings()).validate()
val res3 = Guitar(model = "Really new model", strings = GuitarString.newStrings()).validate()

// flattens the results of the three validations together and return a
// ValidatedNel<Errors, Triple<Guitar, Guitar, Guitar>> that is able to accumulate errors on those.
fun validateGuitars(): ValidatedNel<GuitarErrors, Triple<Guitar, Guitar, Guitar>> =
    // we combine the results into a Triple
    res1.zip(res2, res3) { res1, res2, res3 -> Triple(res1, res2, res3) }

fun main() {
    // Let's say we want to validate the model of two instruments:
    //val res1 = validateModel(Guitar(model = "Non electric model"))
    //val res2 = validateModel(Drums(model = "Old model"))

    //val res = res1.zip(res2) { a, b -> Pair(a, b) }
    // Invalid(NonEmptyList([Error$NonElectricModel, Error$OldModel]))

    // Invalid(NonEmptyList([Error$NonElectricModel, Error$OldModel]))
    // That would be the result if our validations returned those given errors.

    // Validated provides constructor functions to lift any value into a ValidatedNel:
    //a.validNel() // Valid<NonEmptyList<Error>, A>
    //b.invalidNel() // Invalid<NonEmptyList<Error>, A>
}