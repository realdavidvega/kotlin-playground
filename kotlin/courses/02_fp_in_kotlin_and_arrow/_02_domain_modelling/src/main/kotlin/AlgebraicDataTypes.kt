
// In Math, algebras are defined as "a set of objects plus the operations to perform on those objects that
// allow to create new objects within the algebra".

// An example of algebra could be the algebra of numbers ("objects" are numbers, "operations" are +, -, *, ...).

// For FP, we have the ADTs:
// - Product type: "objects" are types that compose it, and the "operation" could be the constructor of the type,
// that creates a new type out of those types.
// - Sum Type: "objects" are the types that it can take at runtime, "operation" is the sum type structure
// itself that composes those types to create a closed hierarchy.

// But algebras are not necessarily used to model data only, they can be used to model operations in our domain.
sealed class SpeakerServiceAlgebra {
    data class LoadById(val id: String) : SpeakerServiceAlgebra()
    object LoadAll : SpeakerServiceAlgebra()
    data class CreateOrUpdate(val speaker: Speaker) : SpeakerServiceAlgebra()
}

// We have a set of operations that a service could perform represented as a combination of sum types and
// product types (ADTs). The algebra is agnostic of the implementation details, and an interpreter is required.
//fun interpret(algebra: SpeakerServiceAlgebra, api: SpeakerApiClient) {
//    when (algebra) {
//        is LoadById -> api.loadById(algebra.id)
//        SpeakerServiceAlgebra.LoadAll -> api.loadAll()
//        is CreateOrUpdate -> api.createOrUpdate(algebra.speaker)
//    }
//}

// If we take SpeakerApiClient as an implementation detail, the interpreter gives meaning to each one of the
// operations by providing the implementation details for it. Imagine if SpeakerApiClient lived in a library module
// that our business wanted to be agnostic of.

data class SpeakerPerson(val id: String, val name: String)

/**
 * Our algebra. A family of operations to persist and query for SpeakerPersons.
 */
sealed class SpeakerPersistence<out A> {
    data class LoadById(val id: String) : SpeakerPersistence<SpeakerPerson>()
    object LoadAll : SpeakerPersistence<List<SpeakerPerson>>()
    data class CreateOrUpdate(val speaker: SpeakerPerson) : SpeakerPersistence<SpeakerPerson>()

    // We extend the SpeakerPersistence algebra to add a new operation called Delete that contains a speaker id
    // to find and delete a Speaker.
    data class Delete(val id: String) : SpeakerPersistence<Boolean>()
}

/**
 * This database driver is a fake in memory representation. Our test will check its state after each interpreter call.
 */
class DBDriver {
    private val speakers = mutableSetOf<SpeakerPerson>()

    fun createOrUpdate(speaker: SpeakerPerson): SpeakerPerson {
        speakers.add(speaker)
        return speaker
    }

    fun loadAll(): List<SpeakerPerson> {
        return speakers.toList()
    }

    fun loadById(id: String): SpeakerPerson? {
        return speakers.find { it.id == id }
    }

    fun delete(id: String): Boolean {
        return speakers.removeIf { it.id == id }
    }

    fun reset(): Unit {
        speakers.clear()
    }
}

/**
 * This interpreter binds each one of the operations in the algebra to its implementation details.
 */

// This function runs the corresponding operation in the DBDriver for each case.
// Keep in mind that the result of each operation in the algebra is specified by the generic type A
// (E.g: LoadAll : SpeakerPersistence<List<Speaker>>() has a List<Speaker> as return type).
// That means whenever we interpret the operation and bind it to implementation details, the details must return
// that type. Given all operations return different types, Kotlin will cast all branches of a when statement to a
// common parent when used as an expression, so it'll cast everything to Any? here. To avoid that, we need to add an
// explicit "as A" at the end of when block, so the function always returns the required type for each case.
fun <A> interpreter(algebra: SpeakerPersistence<A>, db: DBDriver): A =
    when (algebra) {
        is SpeakerPersistence.CreateOrUpdate -> db.createOrUpdate(algebra.speaker)
        is SpeakerPersistence.Delete -> db.delete(algebra.id)
        SpeakerPersistence.LoadAll -> db.loadAll()
        is SpeakerPersistence.LoadById -> db.loadById(algebra.id)
    } as A
