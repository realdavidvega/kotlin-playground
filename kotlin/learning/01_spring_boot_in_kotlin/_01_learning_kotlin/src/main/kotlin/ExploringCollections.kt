package main.kotlin

fun main() {
    // lists in kotlin
    val colors = listOf("Red", "Green", "Blue")

    // not recognized by the compiler as this is an immutable list
    //colors.add

    // we can access with same notation as in java
    println(colors[0])

    // this works as it is returning to us a new list, it is not modifying the original one
    println(colors.filter { it === "Red" })

    // let's see what is really colors... and its still a ArrayList!
    println(colors::class.qualifiedName)

    // right now using listOf is creating an immutable ArrayList, removing methods like .add
    // for now under the hood it is still using java ArrayList, but maybe in future it is a different implementation
    // also, type is inferred if we provide the values
    val days = mutableListOf("Monday", "Tuesday", "Wednesday")
    println(days[0])
    println(days::class.qualifiedName)

    // if we do not provide the values, we need to specify the data type
    val numbers = mutableListOf<Int>()
    println(numbers)

    // immutable sets in kotlin
    val months = setOf("Jan", "Feb")
    months.forEach { println(it) }

    // mutable sets
    val otherMonths = mutableSetOf("Mar", "Apr")
    otherMonths.forEach { println(it) }

    // immutable maps in kotlin
    val webColors = mapOf("red" to "ff0000", "blue" to "0000ff")
    webColors.forEach { (println(it)) }

    // mutable maps (empty)
    val otherWebColors = mutableMapOf<String, String>()
    otherWebColors["red"] = "ff0000"
    otherWebColors["green"] = "00ff00"

    // immutable linked hash maps (ordered version, maintains order always)
    val names = linkedMapOf(1 to "David", 2 to "Jose")
    names[0] = "Alberto"
    names.forEach { (println(it)) }

    // arrays in kotlin, and they are always mutable
    // also their size is fixed, we cannot add or remove
    val intArray = arrayOf(1, 2, 3, 4, 5)

    // equivalent to below
    //intArray.set(0, 5)
    intArray[0] = 5
    println(intArray[0])

    // other kind of array
    val otherIntArray: IntArray = intArrayOf(1, 2, 3)
    intArray[0] = -1
    intArray[2] = -3
    otherIntArray.forEach { println(it) }

    // arrays are more performant than lists however its more specialist requirement
    // most times, we will use more lists as they provide more functionality
}