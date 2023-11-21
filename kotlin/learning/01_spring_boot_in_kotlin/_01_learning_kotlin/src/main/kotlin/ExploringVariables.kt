package main.kotlin

import java.math.BigDecimal
import kotlin.math.roundToInt

// void functions return Unit, but it is not needed
fun main() {
    // ---
    // Strings, variables and println
    // ---

    // Hello World, semicolon is not needed
    println("Hello World")

    // Mutable
    var name: String = "Matt"
    name = "John"

    // Immutable
    val surname: String = "Greencroft"
    //surname = "Something else"

    // Kotlin functions from kotlin.io package
    // Using string templates
    println("$name ${surname.uppercase()}")

    // Length is an attribute, not a method
    println("Your main.kotlin.first name has ${name.length} characters")

    // Not a variable
    println("Your product cost $10")

    // Playing with println
    println("The \$name variable has value $name")

    // Multiline string
    val story = """It was a dark and stormy night.
        |A foul smell crept across the city.
        |Jane wondered what time it was, and when it would be daylight.""".trimMargin("|")

    // Will get indented if we don't use the | character and trimMargin
    println(story)

    // Replace after the last occurrence of a particular word, but there are more and more functions
    val changedStory = story.replaceAfterLast("it", "would be dawn.")
    println(changedStory)

    // ---
    // Double data type
    // ---

    // Inferred type
    val myDouble = 21.4

    // Check type
    println("Is myDouble a Double? ${myDouble is Double}")

    // Reflection to find out the class
    println("myDouble is a ${myDouble::class.qualifiedName}")

    // Java class, in JVM is a double and in Kotlin is the Double object
    println("myDouble's javaClass is ${myDouble.javaClass}")

    // ---
    // Int data type
    // ---

    // From double to integer
    val myInteger = myDouble.roundToInt()
    println("myInteger is a ${myInteger::class.qualifiedName}")

    // java.lang.Integer != Kotlin.Int, so it won't compile
    //val anotherInteger: Integer = 17

    // Compiles, as Kotlin uses Int by default
    val oneInteger = 17
    val anotherInteger: Int = 17

    // ---
    // Other data types
    // ---

    // Float
    val myFloat: Float = 13.6f

    // Same math operations as java
    val result = myFloat + anotherInteger;
    println(result)

    // Big decimal in Kotlin, we don't need 'new' to instantiate like in java
    val bd: BigDecimal = BigDecimal(17)

    // This also works of course
    val otherBd = BigDecimal(20)

    // Variables in kotlin can't be used if they are not initialized
    val anotherBd: BigDecimal

    // Won't compile
    //println(anotherBd.abs())

    // But here we are initializing it main.kotlin.first time, so it compiles
    anotherBd = bd.add(BigDecimal(30))

    // And of course we cannot assign it again, so it won't compile
    //anotherBd = bd.add(BigDecimal(10))

    // Unit object for void functions, not null
    val myUnit: Unit
}
