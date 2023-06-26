package main.kotlin

import java.math.BigDecimal

// we get this code from exercise2
data class Seat(val row: Int, val num: Int, val price: BigDecimal, val description: String) {
    override fun toString(): String = "Seat $row-$num $$price ($description)"
}

// you can create exceptions in kotlin extending from other exceptions
class UnauthorizedUserException(val authorizationKey: String) : Throwable()

interface BookingManager {
    // we can create variables in the interface, and they must be implemented on the class
    // so the version must be provided
    val version: String

    fun isSeatFree(seat: Seat): Boolean
    fun reserveSeat(seat: Seat, customerID: Long): Boolean

    // default implementation in kotlin
    fun systemStatus() = "All Operations are Functional"
}

// syntax of implementing an interface
// classes in kotlin are final by default, and we cannot extend them unless we made them 'open'
open class BasicBookingManager(authorizationKey: String): BookingManager {
    // adding it to the constructor would also work of course
    // it would override the version adding it to the constructor with override and giving a default value
    //open class BasicBookingManager(override val version: String = "1.0"): BookingManager

    override val version = "1.0"

    // we implement the functions
    // we don't need to implement systemStatus as it is implemented by default
    override fun isSeatFree(seat: Seat) = true
    override fun reserveSeat(seat: Seat, customerID: Long) = false

    // primary constructor
    init {
        if (authorizationKey != "12345") throw UnauthorizedUserException(authorizationKey)
    }
}

// extending open classes, you need to call its constructor
// also, in kotlin you can also implement multiple interfaces
class AdvancedBookingManager: BasicBookingManager("12345"), java.io.Closeable {
    // this would also work of course
    //class AdvancedBookingManager: BasicBookingManager(authorizationKey = "1234") {

    override val version = "2.0"

    fun howManyBookings() = 10

    // we implement it just to make it compile on the example
    override fun close() {
        TODO("Not yet implemented")
    }
}

// we can also add functions to existing classes, using extension functions using the following syntax
// after this, String in the whole package will have this new function
fun String.toSentenceCase(): String {
    return this[0].uppercase() + this.substring(1)
}

fun main() {
    // we create a Seat and create an instance of the extended class, calling the inherited fun
    val someSeat = Seat(1, 1, BigDecimal.ZERO, "")
    println(AdvancedBookingManager().isSeatFree(someSeat))

    // this should work as it matches the if
    BasicBookingManager("12345")

    // we check if the exception is thrown and return the failing authorizationKey
    try {
        BasicBookingManager("1234")
    } catch(e: UnauthorizedUserException) {
        println(e.authorizationKey)
    }

    // if you see there are new methods added by kotlin, they did this adding extension functions
    //val myList = mutableListOf<Int>()
    //myList.add()

    // now Strings have toSentenceCase() because we added it
    val greeting = "welcome to the system"
    println(greeting.toSentenceCase())
}
