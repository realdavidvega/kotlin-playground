package com.springkotlindemo.theater.service

import com.springkotlindemo.theater.domain.Seat
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TheaterService {
    private val hiddenSeats = mutableListOf<Seat>()

    init {
        fun getCharRow(rowNum: Int) = (rowNum + ASCII_OFFSET).toChar()

        fun getPrice(rowNum: Int, num: Int) : BigDecimal {
            return when {
                rowNum >= BACK_ROW -> BigDecimal(CHEAPER_ROW_PRICE)
                num <= MID_ROW_LOW || num >= MID_ROW_HIGH -> BigDecimal(MID_ROW_PRICE)
                rowNum == 1 -> BigDecimal(FIRST_ROW_PRICE)
                else -> BigDecimal(OTHER_ROW_PRICE)
            }
        }

        fun getDescription(rowNum: Int, num: Int) : String {
            return when {
                rowNum == BACK_ROW -> "Back Row"
                rowNum == CHEAPER_ROW -> "Cheaper Seat"
                num <= MID_ROW_LOW || num >= MID_ROW_HIGH -> "Restricted View"
                rowNum <= 2 -> "Best View"
                else -> "Standard Seat"
            }
        }
        for (rowNum in 1..MAX_ROW_NUM) {
            for (num in 1..MAX_SEAT_NUM) {
                hiddenSeats.add(
                    Seat(0, getCharRow(rowNum), num, getPrice(rowNum,num), getDescription(rowNum,num))
                )
            }
        }
    }

    fun find(num: Int, rowNum: Char): Seat {
        return seats.first { it.rowNum == rowNum && it.num == num }
    }

	val seats
    get() = hiddenSeats.toList()

    companion object {
        // Like val, variables defined with the const keyword are immutable.
        // The difference here is that const is used for variables that are known at compile-time.
        // Declaring a variable const is much like using the static keyword in Java.
        private const val MAX_ROW_NUM = 15
        private const val MAX_SEAT_NUM = 36
        private const val BACK_ROW = 15
        private const val CHEAPER_ROW = 14
        private const val CHEAPER_ROW_PRICE = 14.50
        private const val MID_ROW_LOW = 3
        private const val MID_ROW_HIGH = 34
        private const val MID_ROW_PRICE = 16.50
        private const val FIRST_ROW_PRICE = 21
        private const val OTHER_ROW_PRICE = 18
        private const val ASCII_OFFSET = 64
    }
}
