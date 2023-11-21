package main.kotlin

fun main() {
    // Won't compile if we are putting a type
    //var name: String = null

    // This will compile, it is the Nothing object type
    // It has no instances and can represent a value that never exists
    var address = null

    // Won't compile as Nothing can't have a value
    //address = "hello"

    // Int not a nullable Int
    var myInteger = 7

    // Non-nullable, won't compile
    //myInteger = null

    // Nullable string
    var nullName: String? = null

    // Not initialized (but not null)
    var otherName: String?

    // Won't compile, null check
    //println(nullName.uppercase())

    // Can't use it as it is not initialized
    //println(otherName.uppercase())

    // Compiles, smart cast
    nullName = "Matt"
    println(nullName.uppercase())

    var someNullName: String? = null

    // We check it is not null, so it will compile
    if (someNullName != null) {
        // Compiles, smart cast
        println(someNullName.uppercase())
    }

    // It compiles as it is not risk of getting a null pointer exception
    // Maybe it's not what we want, but at least it compiles and does not fail
    println("$someNullName".uppercase())

    // Null safe operator, it will return null (uppercase won't run)
    println(someNullName?.uppercase())

    // Equivalent to
    val result: String?
    result = if (someNullName != null) {
        someNullName.uppercase()
    } else {
        null
    }

    someNullName = "no longer null"
    // Null safe operator, but this case it will uppercase it
    println(someNullName?.uppercase())

    // Non-null asserted operator
    // This variable even if its nullable, we are ensuring to Kotlin it is not null
    // CAUTION: it can give us a null pointer exception
    // So it's better not to use it if possible
    val otherResult = someNullName!!.uppercase()
}