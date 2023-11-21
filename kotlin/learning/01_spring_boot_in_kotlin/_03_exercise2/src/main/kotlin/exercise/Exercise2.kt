package main.kotlin.exercise

import java.math.BigDecimal

data class Seat(val row: Int, val num: Int, val price: BigDecimal, val description: String) {
	//YOU MAY NOT EDIT THIS CLASS!
    override fun toString(): String = "Seat $row-$num $$price ($description)"
}

class Theater {
    // REQUIREMENTS
    // 15 rows, 36 sets per row

   // SEAT PRICES:
   // Seats in rows 14 and 15 cost 14.50
   // Seats in rows 1 to 13 and numbered 1 to 3 or 34 to 36 cost 16.50
   // All other seats in row 1 cost 21.00
   // All other seats cost 18.00
   
   // SEAT DESCRIPTIONS:
   // Seats in row 15: "Back row"
   // Seats in row 14: "Cheaper seat"
   // All other rows, seats 1 to 3 and 34 to 36: "Restricted View"
   // All other seats in rows 1 and 2 "Best view"
   // All other seats: "Standard seat"

    //THIS MUST BE AN IMMUTABLE LIST
	val seats = listOf<Seat>() // remove this part
}

fun main() {
    val cheapSeats = Theater().seats.filter {it.price == BigDecimal(14.50) }
    for (seat in cheapSeats) println (seat)
}