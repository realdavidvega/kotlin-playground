package com.springkotlindemo.theater.controller

import com.springkotlindemo.theater.domain.Booking
import com.springkotlindemo.theater.domain.Performance
import com.springkotlindemo.theater.domain.Seat
import com.springkotlindemo.theater.repository.PerformanceRepository
import com.springkotlindemo.theater.repository.SeatRepository
import com.springkotlindemo.theater.service.BookingService
import com.springkotlindemo.theater.service.TheaterService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.ModelAndView

@Controller
class MainController(
    var theaterService: TheaterService,
    var bookingService: BookingService,
    var seatRepository: SeatRepository,
    var performanceRepository: PerformanceRepository,
    ) {
    /*
    // request mapping for helloWorld
    @RequestMapping("helloWorld")
    fun helloWorld(): ModelAndView {
        // this will match the HTML on templates folder
        return ModelAndView("helloWorld")
    }
    */

    /*
    // Dependency injection
    @Autowired
    lateinit var theaterService: TheaterService
    */

    @RequestMapping("")
    fun homePage(): ModelAndView {
    val model = mapOf(
        "bean" to CheckAvailabilityBackingBean(),
        "performances" to performanceRepository.findAll(),
        "seatNums" to 1..36,
        "seatRows" to 'A'..'O'
    )
        //return ModelAndView("seatBooking", "bean", CheckAvailabilityBackingBean())
        return ModelAndView("seatBooking", model)
    }

    @RequestMapping("checkAvailability", method = [RequestMethod.POST])
    fun checkAvailability(bean: CheckAvailabilityBackingBean): ModelAndView {
        //val selectedSeat = theaterService.find(bean.selectedSeatNum, bean.selectedSeatRow)
        //bean.result = "Seat $selectedSeat is " + if (result) "available" else "booked"
        val selectedSeat = bookingService.findSeat(bean.selectedSeatNum, bean.selectedSeatRow)!!
        val selectedPerformance = performanceRepository.findById(bean.selectedPerformance!!).get()
        bean.seat = selectedSeat
        bean.performance = selectedPerformance

        val result = bookingService.isSeatFree(selectedSeat, selectedPerformance)
        bean.available = result

        if (!result) {
            bean.booking = bookingService.findBooking(selectedSeat, selectedPerformance)
        }

        val model = mapOf(
            "bean" to bean,
            "performances" to performanceRepository.findAll(),
            "seatNums" to 1..36,
            "seatRows" to 'A'..'O'
        )

        return ModelAndView("seatBooking", model)
    }

    @RequestMapping("booking", method = [RequestMethod.POST])
    fun bookASeat(bean: CheckAvailabilityBackingBean): ModelAndView {
        val booking = bookingService.reserveSeat(bean.seat!!, bean.performance!!, bean.customerName)
        return ModelAndView("bookingConfirmed", "booking", booking)
    }

    @RequestMapping("bootstrap")
    fun createInitialData(): ModelAndView {
        //create the data and save it to the database
        val seats = theaterService.seats
        seatRepository.saveAll(seats)

        return homePage()
    }
}

class CheckAvailabilityBackingBean {
    var selectedSeatNum: Int = 1
    var selectedSeatRow: Char = 'A'
    var selectedPerformance: Long? = null
    var customerName: String = ""

    var available: Boolean? = null
    var seat: Seat? = null
    var performance: Performance? = null
    var booking: Booking? = null
}

class BackingBeanExample {
    val seatNums =  1..MAX_SEAT_NUM
    val seatRows = 'A'..'O'

    // IMPORTANT: in the form we are sending the selectedSeatNum and selectedSeatRow
    // so, those variables should be var, or otherwise we won't be able of modifying them.
    // That's because Spring will use set methods to do so.
    var selectedSeatNum: Int = 1
    var selectedSeatRow: Char = 'A'
    var result: String = ""

    companion object {
        private const val MAX_SEAT_NUM = 36
    }
}
