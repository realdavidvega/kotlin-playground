package com.springkotlindemo.theater.repository

import com.springkotlindemo.theater.domain.Booking
import com.springkotlindemo.theater.domain.Performance
import com.springkotlindemo.theater.domain.Seat
import org.springframework.data.jpa.repository.JpaRepository

interface SeatRepository : JpaRepository<Seat, Long>
interface PerformanceRepository : JpaRepository<Performance, Long>
interface BookingRepository : JpaRepository<Booking, Long>
