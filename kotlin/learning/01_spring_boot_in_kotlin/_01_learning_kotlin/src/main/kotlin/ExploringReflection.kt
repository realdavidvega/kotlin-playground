package main.kotlin

import kotlin.math.roundToInt
import kotlin.math.sqrt

fun isPrime(a: Int): Boolean {
    val maxNumberToCheck = sqrt(a.toDouble()).roundToInt()
    for (i in 2..maxNumberToCheck) if (a % i == 0) return false
    return true
}

fun main() {
    val myList = listOf(1, 2, 3)

    // syntax of reflection
    myList::class.qualifiedName

    // after :: we get the references of the methods
    val filterMethod = myList::filter
    println(filterMethod)

    val otherList = listOf(14, 15, 16, 17, 18, 19, 20)
    val primeNumbers = otherList.filter { isPrime(it) }
    println(primeNumbers)

    // we can also do this using reflection, so we are passing a pointer to that function (not an instance)
    val primeNumbers2 = otherList.filter (::isPrime)
    println(primeNumbers2)

    // list of the declared functions
    //val functions = myList::class.declaredFunctions
}