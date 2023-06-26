import models.Band
import models.BandMember
import models.BandMembers
import models.Instrument
import java.io.IOException
import kotlin.jvm.Throws

// The Kotlin type system can be leveraged to reflect some scenarios in our code.

// We could use Unit as a return type as a means to reflect a side effect, since a function that returns Unit
// is a function that must be consuming its input somehow.
fun handleError(error: Throwable): Unit {
    // Must do something with the error!
}

// At the same time, Nothing can be used to leverage type inference combined with declaration-site variance.
// That means you can annotate the type parameter A of Maybe to make sure that it is only returned (produced)
// from members of Maybe<A>, and never consumed. To do this, use the out modifier.
// See how we need to add the out to make the type A covariant. That means operations like asMaybe are possible.
// If we didn't, Maybe.Absent would never be considered a child of Maybe<A> in an out (return) position,
// hence types would not match.
sealed class Maybe<out A> {
    data class Present<A>(val a: A) : Maybe<A>()
    object Absent : Maybe<Nothing>()
}
fun <A> A?.asMaybe(): Maybe<A> = if (this == null) Maybe.Absent else Maybe.Present(this)

sealed class Result<out A, out B> {
    data class A<A>(val a: A) : Result<A, Nothing>()
    data class B<B>(val b: B) : Result<Nothing, B>()
}

class BandCache {
    @Throws(IOException::class)
    fun fetchBands(): List<Band> = throw IOException("Boom!")
}

// loadBands function calls service.fetchBands() and return Result.B with the loaded bands in case they are loaded
// successfully, or Result.A with an exception in case the operation throws.
// Note that even if this service is stubbed in memory, it could be replaced by a real network service that could
// potentially throw under real scenarios. We are only mimicking that here to showcase the need to make our code
// resilient when working with third parties.
fun loadBands(service: BandCache): Result<Throwable, List<Band>> =
    try {
        val bands = service.fetchBands()
        // if it goes ok, we lift/wrap it into B
        Result.B(bands)
    } catch (e: IOException) {
        // if it goes bad, we lift/wrap it into A
        Result.A(e)
    }

// handleBandsResult depending on its type we call showErrorPopup or showBands callbacks.
fun handleBandsResult(
    result: Result<Throwable, List<Band>>,
    showErrorPopup: (Throwable) -> Unit,
    showBands: (bands: List<Band>) -> Unit
): Unit {
    when(result) {
        is Result.A -> showErrorPopup(result.a)
        is Result.B -> showBands(result.b)
    }
}

fun showErrorPopup(throwable: Throwable): Unit {
    print("Duh! Got a throwable: $throwable")
}

fun showBands(bands: List<Band>): Unit {
    bands.forEach { println(it) }
}

fun main() {
    val service = BandCache()
    // it will boom
    //val result = service.fetchBands()

    // we get the value wrapped
    val bands = loadBands(service)
    println(bands)

    // we handle the value
    handleBandsResult(bands, ::showErrorPopup, ::showBands)
}
