package utilities

// pure function that does something
fun toSentenceCase(input: String): String =
    input[0].uppercase() + input.substring(1)

fun main() {
    println(toSentenceCase("hello"))
}
