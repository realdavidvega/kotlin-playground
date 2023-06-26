package main.kotlin

import java.util.*

data class KotlinPerson(
    val id: Int,
    val title: String,
    val name: String,
    val surname: String,
    val dateOfBirth: Calendar?
    ) {

    override fun toString() = "$title $name $surname"

    val safeAge: Int
        get() {
            // smart cast should be fine, so non-null assertion operator is fine
            //return if (age != null) age!! else -1

            // other option would be creating a local variable and assigning age to it
            //val localAge = age
            //return if (localAge != null) localAge else -1

            // probably best option is using elvis operator for null-safety
            // if it is null, we return -1, if not, we return age
            return age ?: -1
        }

    val age: Int?
        get() = getAge(dateOfBirth)

    companion object {
        fun getAge(dateOfBirth: Calendar?): Int? {
            if (dateOfBirth == null) return null
            val today = GregorianCalendar()
            val years = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR)
            return if (dateOfBirth.get(Calendar.DAY_OF_YEAR) >= today.get(Calendar.YEAR)) years -1 else years
        }
    }

    var favoriteColor: String? = null

    fun getUpperCaseColor(): String {
        // will give us an error
        //return if (favoriteColor == null) "" else favoriteColor.uppercase()

        // other way using elvis operator
        return favoriteColor?.uppercase() ?: "CYAN"
    }

    fun getLastLetter(a: String) = a.takeLast(1)

    fun getLastLetterOfColor(): String {
        // Smart cast to 'String' is impossible, because 'favoriteColor' is a mutable property
        // that could have been changed by this time
        // return if (favoriteColor == null) "" else getLastLetter(favoriteColor)

        // this won't work
        //return getLastLetter(favoriteColor) ?: ""

        // this either, it will give a null pointer exception
        //return getLastLetter(favoriteColor!!) ?: ""

        // we need to use let, all objects in kotlin have the let function with a lambda called it
        // if favoriteColor is not null, we use the let function to run some code
        // the code we run is the getLastLetter with the word it, which is equivalent to x -> getLastLetter(x)
        // and if that is null, with elvis operator we will return an empty string
        // this is used for null check
        return favoriteColor?.let { getLastLetter(it) } ?: ""
    }

    // if, else if without brackets
    fun getColorType(): String {
        val color = getUpperCaseColor()
        return if (color == "")
            "empty"
        else if (color == "RED" || color == "BLUE" || color == "GREEN")
            "rgb"
        else
            "other"
    }

    // alternative to if, else if, else
    // more readable and concise than traditional if else
    fun getColorTypeUsingWhen(): String {
        return when (getUpperCaseColor()) {
            "" -> "empty"
            "RED", "BLUE", "GREEN" -> "rgb"
            else -> "other"
        }
    }

}

// short return if else
fun isNameDavid(name: String): String {
    return if (name == "David") {
        "yes"
    } else {
        "no"
    }
}

fun main() {
    println("Is name David?")
    println(isNameDavid("David"))

    val john = KotlinPerson(1, "Mr", "John", "Cena", GregorianCalendar(1996, 9, 3))
    val jane = KotlinPerson(2, "Ms", "Jane", "Cena", null)

    // even shorter return if else
    val olderPerson = if (john.safeAge > jane.safeAge) john else jane
    println("The older person was $olderPerson")

    john.favoriteColor = "red"
    println("$john favorite color is ${john.getUpperCaseColor()}")
    println("$jane favorite color is ${jane.getUpperCaseColor()}")

    println("$john color type is ${john.getColorType()}")
    println("$jane color type is ${jane.getColorTypeUsingWhen()}")
}
