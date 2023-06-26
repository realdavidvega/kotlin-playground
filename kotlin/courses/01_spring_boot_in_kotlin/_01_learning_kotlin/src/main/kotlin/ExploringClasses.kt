package main.kotlin;

import java.util.*

// Getters and setters in kotlin do exist but in the background
class DefaultCustomer {
    val name: String = "Matt"
    val address: String = "10 The High Street"
    val age: Int = 22
}

class JavaWayCustomer {
    val name: String
    val address: String
    var age: Int

    constructor(name: String, address: String, age: Int) {
        this.name = name
        this.address = address
        this.age = age
    }
}

// Kotlin primary constructor
class Customer(val name: String, val address: String, var age: Int) {

    // Secondary constructor, vals no vars
    // must run the primary constructor on specific syntax
    constructor(name: String, age: Int) : this(name, "", age)

    // we don't need brackets for code blocks, but we can add it
    constructor(name: String, address: String) : this(name, address, 20) {
        println("secondary constructor with code block")
    }

    // runs on primary constructor (also when secondary calls it!)
    init {
        println("init block")
    }
}

// alternative class design
class AlternativeCustomer (val name: String, var age: Int) {
    var address: String

    // won't work, because it's a val, so it is immutable after assignation at the init
    //val address: String

    // we can do this
    init {
        address = ""
    }

    constructor(name: String, address: String, age: Int): this(name, age) {
        this.address = address;
    }
}

// same as main.kotlin.first Customer version
class AnotherAlternativeCustomer(val name: String, var age: Int, val address: String = "")


// overriding getters and setters
class AnotherAlternativeCustomerOverride(val name: String, var age: Int, val address: String = "") {
    var approved: Boolean = false
    set (value) {
        if (age >= 21) {
            field = value
        } else {
            println("You can't approve a customer under 21 years old")
        }
    }

    // dummy variable using a getter
    val nextAge
        get () = age + 1

    // same as data class, in order to expose in destructuring
    operator fun component1() = name
    operator fun component2() = age

    fun uppercaseName(): String {
        return name.uppercase(Locale.getDefault());
    }

    // shorter version in one-line function
    fun uppercaseNameShorter() =
        name.uppercase(Locale.getDefault())

    // override functions
    override fun toString() =
        "$name $address $age"

    // static function using companion objects like a factory method to create objects
    companion object {
        fun getInstance() = AnotherAlternativeCustomerOverride("Micky", 22, "Some address")
    }
}

// we get getters, setters, toString, hashCode...
data class DataCustomer(val name: String, var age: Int, val address: String = "")

fun main() {
    val defaultCustomer = DefaultCustomer()
    println("${defaultCustomer.name} is ${defaultCustomer.age} years old")

    val javaWayCustomer = JavaWayCustomer("Matt", "10 The High Street", 21)
    javaWayCustomer.age = 22
    println("${javaWayCustomer.name} is ${javaWayCustomer.age} years old")

    val customer = Customer("Matt", "10 The High Street", 22)
    println("${customer.name} is ${customer.age} years old")

    val customer2 = Customer("John", 31)
    println("${customer2.name} is ${customer2.age} years old")

    val customer3 = Customer("John", "10 The High Street")
    println("${customer3.name} is ${customer3.age} years old")

    val alternativeCustomer = AlternativeCustomer("John", 12)
    println("${alternativeCustomer.name} is ${alternativeCustomer.age} years old")

    val anotherCustomer = AnotherAlternativeCustomer("John", 21)
    println("${anotherCustomer.name} is ${anotherCustomer.age} years old")

    val overrideCustomer = AnotherAlternativeCustomerOverride("John", 20)
    overrideCustomer.approved = true
    println("${overrideCustomer.name} is ${overrideCustomer.age} years old")
    println("next year ${overrideCustomer.name} will be ${overrideCustomer.nextAge}")
    println("next year ${overrideCustomer.uppercaseNameShorter()} will be ${overrideCustomer.nextAge}")
    println("the customer is: $overrideCustomer")

    val overrideCustomer2 = AnotherAlternativeCustomerOverride.getInstance()
    println(overrideCustomer2)

    val dataCustomer = DataCustomer("Sally", 29)
    println(dataCustomer)
    println(dataCustomer.hashCode())

    // thanks to data class we also get the copy method
    val dataCustomer2 = dataCustomer.copy(name = "Diane")
    println(dataCustomer2)

    // Destructuring on data class
    val (name, age, address) = dataCustomer
    println("$name $age $address")
}
