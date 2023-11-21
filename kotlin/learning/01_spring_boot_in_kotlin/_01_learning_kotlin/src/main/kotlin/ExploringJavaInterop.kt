package main.kotlin

import kotlin.jvm.Throws

fun someTopClassFunction() = println("Hi, I am here")

data class KotlinCustomer(val id: Long, val name: String)

class KotlinCustomerDatabase() {
    val kotlinCustomers = listOf(
        KotlinCustomer(1, "Matt"),
        KotlinCustomer(2, "James"),
        KotlinCustomer(3, "Dianne"),
        KotlinCustomer(4, "Sally"),
    )

    fun addCustomer(c: KotlinCustomer) {
        throw IllegalAccessException("You cannot add a customer")
    }

    // for java
    @Throws(IllegalAccessException::class)
    fun addCustomerForJava(c: KotlinCustomer) {
        throw IllegalAccessException("You cannot add a customer")
    }

    // static functions
    companion object {
        fun helloWorld() = println("Hello world")

        // with this annotation, we would be able of calling the static method from Java without having to access
        // the Companion object
        @JvmStatic
        fun greetings() = println("Greetings")
    }

    // -- For importing a Kotlin JAR into a Java Project --
    // We can export this to a file using IntelliJ: File -> Project Structure -> Artifacts
    // Click on + -> JAR -> From modules with dependencies...
    // Select the file -> no main class -> OK -> Apply
    // Build menu -> build artifacts, and it will build it at workspace -> out -> artifacts
    // Then go to Java project -> copy JAR file in lib/
    // Right click -> add as library and start importing and using it!
}