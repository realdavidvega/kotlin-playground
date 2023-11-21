package main.kotlin.solution

import main.kotlin.exercise.Seat
import java.math.BigDecimal

class Theater() {
    private val hiddenSeats = mutableListOf<Seat>()

    // could also be constructor()
    init {
        // function inside the primary constructor, which is not visible to anyone else
        fun getPrice(row: Int, num: Int): BigDecimal {
            return when {
                row in 14..15 -> BigDecimal(14.5) // we could also use row >= 14, same for rest of cases
                num in 1..3 || num in 34..36 -> BigDecimal(16.5)
                row == 1 -> BigDecimal(21)
                else -> BigDecimal(18)
            }
        }

        fun getDescription(row: Int, num: Int): String {
            return when {
                row == 15 -> "Back row"
                row == 14 -> "Cheaper seat"
                num in 1..3 || num in 34..36 -> "Restricted view"
                row in 1..2 -> "Best view"
                else -> "Standard seat"
            }
        }

        for (row in 1..15)
            for (num in 1..36)
                hiddenSeats.add(Seat(row, num, getPrice(row, num), getDescription(row, num)))
    }

    val seats
        get() = hiddenSeats.toList()
}

fun main() {
    val cheapSeats = Theater().seats.filter {it.price == BigDecimal(14.50) }
    for (seat in cheapSeats) println (seat)
}