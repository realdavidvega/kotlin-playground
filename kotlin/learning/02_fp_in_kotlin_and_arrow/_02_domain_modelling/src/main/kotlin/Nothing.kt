
// Nothing is the bottom type in Kotlin, meaning it effectively extends from any other types in the system.
// It doesn't physically extend all types, but the compiler understands it that way.
// That means we can use it in a return position as a placeholder where another type is expected.

data class Spy(var called: Boolean = false)

/* Our custom TODO function for the sake of the example */
fun placeholder(spy: Spy): Nothing {
    spy.called = true
    throw NotImplementedError("This code is not implemented!")
}

sealed class State<out A> {
    data class Error(val e: Exception) : State<Nothing>()
    data class Success<A>(val a: A) : State<A>()
    object Cancelled : State<Nothing>()
}

// The variance modifiers out and in allow us to make our generic types less restrictive and
// more reusable by allowing subtyping.

// out produces T and preserves subtyping
//When you declare a generic type with an out modifier, it's called covariant.
// A covariant is a producer of T, that means functions can return T but, they can't take T as arguments

/**
 * Imagine a simple UI function that wants to translate the state to its human-readable version.
 */
fun <A> humanReadableState(state: State<A>, spy: Spy): String = when (state) {
    State.Cancelled -> "This state is cancelled! You can try loading it again."
    is State.Error -> "Error: ${state.e.localizedMessage}"
    is State.Success -> "Successful state: ${state.a}"
}

// We can replace the cases String returned by humanReadableState by a call to our placeholder function.
// Note how all the types are still matching, even if the function return type is String.
// That is only because Nothing is the bottom type, so it "extends" from String,
// as it does for any other type in the language.
fun <A> humanReadableStatePlaceholder(state: State<A>, spy: Spy): String = when (state) {
    State.Cancelled -> placeholder(spy)
    is State.Error -> "Error: ${state.e.localizedMessage}"
    is State.Success -> placeholder(spy)
}

fun main() {
    val spy = Spy(false)
    val cancelled = State.Cancelled

    // will return the String defined on the function's when
    val state = humanReadableState(cancelled, spy)
    println(state)

    // will return an exception
    val statePlaceholder = humanReadableStatePlaceholder(cancelled, spy)
}
