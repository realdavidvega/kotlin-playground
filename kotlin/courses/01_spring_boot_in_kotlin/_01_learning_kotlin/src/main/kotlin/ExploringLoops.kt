package main.kotlin

fun main() {

    // while on kotlin is same as java
    var x: Int = 0
    while (x < 10) {
        println("num: $x")
        x++
    }

    //array list like in java, and val makes the reference immutable but not the content
    val people = ArrayList<KotlinPerson>()
    people.add(KotlinPerson(1, "Mr", "James", "Apple", null))
    people.add(KotlinPerson(2, "Ms", "Sophie", "Orange", null))
    people.add(KotlinPerson(3, "Ms", "Anita", "Lemon", null))
    people.add(KotlinPerson(4, "Mr", "Darren", "Banana", null))

    // for loops for collections in kotlin use in instead of : for obvious reasons
    // type can be inferred
    //for (person: KotlinPerson in people) {
    for (person in people) {
        println(person)
    }

    // destructuring in a loop with a data class
    for (person in people) {
        val (id, title) = person
        println("$person has id $id and title $title")
    }

    // we can also to this kind of destructuring
    for ((id, title, name) in people) {
        println("$title $name has id $id")
    }

    // hash maps in kotlin
    val peopleMap = HashMap<Int, KotlinPerson>()
    peopleMap.put(1, KotlinPerson(1, "Mr", "James", "Apple", null))
    peopleMap.put(2, KotlinPerson(2, "Ms", "Sophie", "Orange", null))
    peopleMap.put(3, KotlinPerson(3, "Ms", "Anita", "Lemon", null))
    peopleMap.put(4, KotlinPerson(4, "Mr", "Darren", "Banana", null))

    // we can also do it this way, this is the same as put (we are replacing previous ones with same data)
    peopleMap[1] = KotlinPerson(1, "Mr", "James", "Apple", null)
    peopleMap[2] = KotlinPerson(2, "Ms", "Sophie", "Orange", null)
    peopleMap[3] = KotlinPerson(3, "Ms", "Anita", "Lemon", null)
    peopleMap[4] = KotlinPerson(4, "Mr", "Darren", "Banana", null)

    // iterating keys and values of a hashmap using destructuring
    for ((key, value) in peopleMap) {
        println("$value has key $key")
    }

    // ranges in kotlin and it provides an iterator
    val myRange = 0..9

    // equivalent to for from 0 to 9 java for loop
    for (i in myRange) {
        // do something
        println(i)
    }

    // functional way with lambda
    myRange.forEach{ i -> println(i) }

    // functional way with keyword it
    myRange.forEach { println(it) }

    // creating different types of ranges
    // down from 9 to 0 including 9
    (9 downTo 0).forEach { println(it) }

    // from 0 to 9 without including 9
    (0 until 9).forEach { println(it) }

    // from 0 to 9 in steps of 2
    (0..9 step 2).forEach { println(it) }

    // also with chars
    ('A'..'F').forEach { println(it) }
}