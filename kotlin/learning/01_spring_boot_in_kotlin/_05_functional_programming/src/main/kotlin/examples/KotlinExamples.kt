package examples

import utilities.toSentenceCase

// in kotlin we use this specific syntax (something) -> something
// also we don't need to use apply as we already have the function here we want to apply to
fun applySomeFunctionToAString(inputString: String, myFunction: (String) -> String): String =
    myFunction(inputString)

fun main() {
    // in kotlin when the function is the last parameter, we can provide it in outside with brackets
    val result = applySomeFunctionToAString("hello") { it.uppercase() }
    println(result)

    // we can also define a function like this in kotlin
    val someFunc: (String) -> String = { x -> x[0].uppercase() + x.substring(1)}

    // and apply it like this
    val result1 = applySomeFunctionToAString("hello", someFunc)
    println(result1);

    // same as
    val result2 = applySomeFunctionToAString("hello") { x -> x[0].uppercase() + x.substring(1)}
    println(result2)

    // importing from Java, using reflection syntax
    // this is the reflection syntax, and this is how in kotlin we provide a reference to a lambda function
    val result3 = applySomeFunctionToAString("hello", ::toSentenceCase)
    println(result3)

    // we can also apply it using let keyword as scope function
    val result4 = "hello".let { x -> x[0].uppercase() + x.substring(1) }
    println(result4)

    // equivalent to
    val result5 = "hello".let { it[0].uppercase() + it.substring(1) }
    println(result5)

    // We could achieve the same as in Java for the colors, using the map function and forEach
    val colors = listOf("red", "green", "blue", "black")
    val uppercaseColors = colors.map {it.uppercase()}
    uppercaseColors.forEach { println(it) }

    val colorsStartingWithB = colors.filter { it.startsWith("b") }
    colorsStartingWithB.forEach { println(it) }

    // we take a list, and we create a new list from it, but it could be longer or shorter, it's not a 1 to 1 mapping
    // if starts with b, we duplicate the element in the list
    colors.flatMap { if (it.startsWith("b")) listOf(it, it) else listOf(it) }.forEach { println(it) }

    // reduce to a single value
    val reduced = colors.reduce {result, value -> "$result, $value" }
    println(reduced)

    // some collection functions in kotlin
    val numbers = colors.map { it.length }
    numbers.forEach { println(it) }
    println(numbers.sum())
    println(numbers.average())
    println(numbers.count())

    // fold method, same to implement the sum function
    println(numbers.fold(0) {result, value -> result + value})

    // largest item
    println(numbers.fold(0) {result, value -> if (value > result) value else result})

    // we can also operate with maps
    val myMap = mapOf(1 to "one", 2 to "two", 3 to "three")
    myMap.filter { (_, v) -> v.startsWith("t") }.forEach { (k, v) -> println("$k $v") }

    // if we are not destructuring, we can also do this
    myMap.filter { x -> x.value.startsWith("t") }.forEach { (k, v) -> println("$k $v")}

    // or this
    myMap.filter { it.value.startsWith("t") }.forEach { println("${it.key} ${it.value}") }
}
