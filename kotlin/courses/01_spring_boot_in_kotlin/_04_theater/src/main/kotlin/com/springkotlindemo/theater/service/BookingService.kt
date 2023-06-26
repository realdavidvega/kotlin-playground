package com.springkotlindemo.theater.service

import com.springkotlindemo.theater.domain.Booking
import com.springkotlindemo.theater.domain.Performance
import com.springkotlindemo.theater.domain.Seat
import com.springkotlindemo.theater.repository.BookingRepository
import com.springkotlindemo.theater.repository.SeatRepository
import org.springframework.stereotype.Service

@Service
class BookingService(
    var seatRepository: SeatRepository,
    var bookingRepository: BookingRepository
) {
    fun isSeatFree(seat: Seat, performance: Performance) =
        // if there is none, then it's free
        bookingRepository.findAll().none {
            it.performance == performance && it.seat == seat
        }

    fun findSeat(seatNum: Int, seatRow: Char): Seat? =
        // if there is or if not return null
        seatRepository.findAll().firstOrNull {
            it.num == seatNum && it.rowNum == seatRow
        }

    fun findBooking(seat: Seat, performance: Performance): Booking? =
        bookingRepository.findAll().firstOrNull {
            it.performance == performance && it.seat == seat
        }

    fun reserveSeat(seat: Seat, performance: Performance, customerName: String): Booking {
        val booking =  Booking(0, customerName)
        booking.seat = seat
        booking.performance = performance
        bookingRepository.save(booking)
        return booking
    }
}
