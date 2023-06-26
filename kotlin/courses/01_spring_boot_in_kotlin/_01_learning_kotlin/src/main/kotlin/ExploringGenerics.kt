package main.kotlin

// Generics
// (Additional education not included in the original course)
// More info: https://kotlinlang.org/docs/generics.html#declaration-site-variance

// The variance modifiers out and in allow us to make our generic types less restrictive and
// more reusable by allowing subtyping.

// Let's understand this with the help of contrasting examples. We'll use examples of cases as containers of
// various weapons. Assume that we have the following type hierarchy:

open class Weapon
open class Rifle : Weapon()
class SniperRifle : Rifle()

// out produces T and preserves subtyping
// When you declare a generic type with an out modifier, it's called covariant.
// A covariant is a producer of T, that means functions can return T but, they can't take T as arguments:
class CaseOut<out T> {
    private val contents = mutableListOf<T>()
    fun produce(): T = contents.last()         // Producer: OK
    //fun consume(item: T) = contents.add(item)  // Consumer: Error
}

//The Case declared with the out modifier produces T and its subtypes:
fun useProducer(case: CaseOut<Rifle>) {
    // Produces Rifle and its subtypes
    val rifle = case.produce()
}

// With the out modifier, the subtyping is preserved, so the Case<SniperRifle> is a subtype of Case<Rifle> \
// when SniperRifle is a subtype of Rifle. As a result, the useProducer() function can be called with
// Case<SniperRifle> too:
fun runOut() {
    useProducer(CaseOut<SniperRifle>())               // OK
    useProducer(CaseOut<Rifle>())                     // OK
    //useProducer(CaseOut<Weapon>())                    // Error
    //This is less restrictive and more reusable while producing but our class becomes read only.
}

// List<out T> in Kotlin is equivalent to List<? extends T> in Java.

// in consumes T and reverses subtyping
// When you declare a generic type with an in modifier, it's called contravariant.
// A contravariant is a consumer of T, that means functions can take T as arguments but, they can't return T:
class CaseIn<in T> {
    private val contents = mutableListOf<T>()
    //fun produce(): T = contents.last()         // Producer: Error
    fun consume(item: T) = contents.add(item)  // Consumer: OK
}

// The Case declared with the in modifier consumes T and its subtypes:
fun useConsumer(case: CaseIn<Rifle>) {
    // Consumes Rifle and its subtypes
    case.consume(SniperRifle())
}

// With the in modifier, the subtyping is reversed, so now the Case<Weapon> is a subtype of Case<Rifle>
// when Rifle is a subtype of Weapon. As a result, the useConsumer() function can be called with Case<Weapon> too:
fun runIn() {
    //useConsumer(CaseIn<SniperRifle>())               // Error
    useConsumer(CaseIn<Rifle>())                     // OK
    useConsumer(CaseIn<Weapon>())                    // OK
    // This is less restrictive and more reusable while consuming but our class becomes write only.
}

// List<in T> in Kotlin is equivalent to List<? super T> in Java

// Invariant produces and consumes T, disallows subtyping
// When you declare a generic type without any variance modifier, it's called invariant.
// An invariant is a producer as well as a consumer of T, that means functions can take T as arguments and
// can also return T:
class InvariantCase<T> {
    private val contents = mutableListOf<T>()
    fun produce(): T = contents.last()         // Producer: OK
    fun consume(item: T) = contents.add(item)  // Consumer: OK
}

//The Case declared without in or out modifier produces and consumes T and its subtypes:
fun useProducerConsumer(case: InvariantCase<Rifle>) {
    // Produces Rifle and its subtypes
    case.produce()
    // Consumes Rifle and its subtypes
    case.consume(SniperRifle())
}

//Without the in or out modifier, the subtyping is disallowed, so now neither Case<Weapon> nor
// Case<SniperRifle> is a subtype of Case<Rifle>. As a result, the useProducerConsumer() function can only be
// called with Case<Rifle>:
fun runInvariant() {
    //useProducerConsumer(Case<SniperRifle>())       // Error
    useProducerConsumer(InvariantCase<Rifle>())             // OK
    //useProducerConsumer(Case<Weapon>())            // Error
    //This is more restrictive and less reusable while producing and consuming but, we can read and write.
}

// Another example
// For example in Kotlin you can do things like:
val value : List<Any> = listOf(0,1,2)
//since List signature is List<out T> in Kotlin

// The reasoning is you can mark generic "out" if you're returning it, but never receiving it.
// And you can mark it "in" if you receive it, but never return it.

// Conclusion
// The List in Kotlin is a producer only. Because it's declared using the out modifier: List<out T>.
// This means you cannot add elements to it as the add(element: T) is a consumer function.
// Whenever you want to be able to get() as well as add() elements, use the invariant version MutableList<T>.

fun main() {
    runIn();
    runOut();
    runInvariant();
}
