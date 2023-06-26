package main.kotlin

// Unit is not needed, as it is added by default by Kotlin
// Also functions can be created outside of classes, they are called top-level classes
// They are public and static by default, and we can make it private (only from same package)
private fun printAsString(value: String): Unit {
    println(value)
}

// Single expression function
// Also in single expression functions the type can be inferred
fun addTwoNumbers(one: Double, two: Double) =
    one + two

// Also, all attributes are always immutable (they are vals)
fun printSomeMaths(one: Double, two: Double) {
    println("one + two is ${one + two}")
    println("one - two is ${one - two}")
}

// Optional parameters, not need of overloaded methods like in Java for default value of one of the parameters
fun addThreeNumbers(one: Double, two: Double, three: Double = 3.9) =
    one + two + three

// Functions within a function
fun first(a: String) {
    fun second(b: String) {
        println(b)
    }

    // Only visible from inside, not outside
    second(a)
}

// Taking lambda as parameter, Java-way
fun methodTakesALambda(input: String, action: java.util.function.Function<String, Int>) {
    println(action.apply(input))
}

//Taking lambda as parameter, Kotlin-way
fun methodTakesALambdaKotlin(input: String, action: (String) -> Int) {
    println(action(input))
}

// Don't make main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main.kotlin.main private, or it won't be found (in JVM gets converted to public static void)
fun main() {
    printAsString("Hello world")
    println(addTwoNumbers(1.0, 1.0))
    println(printSomeMaths(1.0, 1.0))
    println(printSomeMaths(1.0, 1.0))

    // Named parameters
    println(printSomeMaths(two = 1.0, one = 1.0))

    // This works
    println(addThreeNumbers(1.0, 2.0))
    println(addThreeNumbers(two = 1.0, one = 1.0))

    first("Hello world")
}