import kotlin.time.Duration

data class Talk(val id: String, val title: String, val duration: Duration)

// Product types are a structurally defined by their properties:
data class Speaker(
    val id: Long,
    val name: String,
    val talks: List<Talk>
)

// In Algebra, we could understand this type as the product of the Long * String * List<Talk> types,
// since all of them need to exist to create the Speaker type. And each one of them can take a different
// amount of values (also known as the type "inhabitants").
// The Kotlin compiler is aware of the product type structure, so it can generate syntax for it.
// I.e: equals, hashCode, toString, copy...

// it performs a "deep equals" to check whether both Speaker instances are equal.
fun Speaker.isEqual(other: Speaker): Boolean =
    this == other

// it transforms the provided Speaker into a readable String listing its properties.
fun Speaker.stringify(): String =
    this.toString()

// it updates the provided Speaker with the new also provided list of talks.
fun Speaker.update(newTalks: List<Talk>): Speaker =
    this.copy(talks = newTalks)
