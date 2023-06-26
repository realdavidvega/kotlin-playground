package main.kotlin

import java.math.BigDecimal
import java.util.*

fun main() {
    // Kotlin equivalent to object
    var result: Any

    // New not needed in Kotlin
    val randomNumber = Random().nextInt(3)

    // Assignment can be lifted out of if in Kotlin, very functional =)
    result = if (randomNumber == 1) {
        BigDecimal(30)
    } else {
        "hello"
    }

    println("Result is currently $result");

    result = if (result is BigDecimal) {
        // Smart cast
        result.add(BigDecimal(47))
    } else {
        // Cast in Kotlin
        val tempResult = result as String
        tempResult.uppercase();
    }

    println("Result is currently $result");
}