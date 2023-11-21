package main.kotlin

import java.util.*

fun main() {
    val date = Calendar.getInstance()
    date.set(1995, 3, 24)

    val person = KotlinPerson(12345, "Engineer", "David", "Vega", date)
    val birthDate = "${date.get(Calendar.DATE)}/${date.get(Calendar.MONTH)}/${date.get(Calendar.YEAR)}"

    println("${person.firstName} ${person.firstName} was birth on $birthDate")

    val john = KotlinPerson(1L, "Mr", "John", "Blue", GregorianCalendar(1977,9,3))
    val jane = KotlinPerson(2L, "Mrs", "Jane", "Green", null)

    println("$john's age is ${john.age}")
    println("$jane's age is ${jane.age}")
    println("The age of someone born on 3rd May 1988 is ${KotlinPerson.getAge(GregorianCalendar(1988,5,3))}")
}