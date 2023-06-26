package main.kotlin

import java.io.FileInputStream
import kotlin.jvm.Throws

fun main() {
    // we can catch exceptions same as in java
    // however, all exceptions in kotlin are unchecked,
    // we are not forced to surround them with try catch as in java
    try {
        println(7 / 0)
    } catch (e: ArithmeticException) {
        println("caught")
    }

    // so we can put this, and it will compile (we don't require it to surround it with try catch)
    // this is made in kotlin to avoid issues with functional programming so the code does not become messy
    // that's why we have to take of them while coding and test more thoughtfully
    Thread.sleep(1000)

    // but we don't get any warning or compilation error
    // so throws exists for java programmers that are calling that function when interacting with kotlin
    divide(5, 23)

    // try statement is also an expression in kotlin
    val result = try {
        divide(5, 23)
    } catch (e: Exception) {
        println(e)
        // if an exception occur, we will return a 0
        0
    }
    println(result)
}

// in kotlin we use an annotation for saying this fun throws an exception
// it is also using the reflection syntax
@Throws(InterruptedException::class)
fun divide(a: Int, b: Int): Double {
    Thread.sleep(1000)
    return (a.toDouble() / b)
}

// use expression in kotlin to ensure resources are always closed
// equivalent of the Java function for printing from a file but in Kotlin
fun printFile() {
    val input = FileInputStream("file.txt")

    // use code block
    input.use {
        // an exception could be thrown here, and then the objects will be automatically closed
        var data = input.read();
        while (data != -1) {
            println(data.toChar())
            data = input.read();
        }
    }
}
