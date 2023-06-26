
//Sum types exist to model choice:
enum class Type {
    STANDARD, PREMIUM
}

// Apply effects depending on the type
fun processType(type: Type): Unit {
    when (type) {
        Type.STANDARD -> { /* doSomething() */ }
        Type.PREMIUM -> { /* doSomethingElse() */ }
    }
}

// In Algebra, they can be defined as the sum of the types that compose them: STANDARD + PREMIUM.

sealed class SomeState<out A> {
    // we are creating new classes extending from the parent class
    data class Error(val e: Exception) : SomeState<Nothing>()
    data class Success<A>(val a: A) : SomeState<A>()
    object Cancelled : SomeState<Nothing>()
}


// Imagine a simple UI function that wants to translate the state to its human-readable version.
// it applies the provided effects depending on the case. This is a very straightforward exercise with the only
// intention to showcase how sum types model choice in a program by imposing branching in caller code,
// which needs to provide actions for handling all possible runtime values of the sum type.
fun <A> processState(
    state: SomeState<A>,
    processCancelled: (SomeState.Cancelled) -> Unit,
    processError: (SomeState.Error) -> Unit,
    processSuccess: (SomeState.Success<A>) -> Unit,
): Unit =
    when(state) {
        is SomeState.Cancelled -> processCancelled(state)
        is SomeState.Error -> processError(state)
        is SomeState.Success -> processSuccess(state)
    }

// unit not needed, however we will put it here
fun cancelled(someState: SomeState<String>): Unit =
    println("cancelled $someState")

fun error(someState: SomeState<String>): Unit =
    println("error $someState")

fun success(someState: SomeState<String>): Unit =
    println("success $someState")

fun main() {
    // without the generic type out A, this wouldn't be possible
    val errorState: SomeState<String> = SomeState.Cancelled

    processState(
        errorState,
        ::cancelled,
        ::error,
        ::success,
    )

    val successState: SomeState<String> = SomeState.Success("some string")

    processState(
        successState,
        ::cancelled,
        ::error,
        ::success,
    )
}
