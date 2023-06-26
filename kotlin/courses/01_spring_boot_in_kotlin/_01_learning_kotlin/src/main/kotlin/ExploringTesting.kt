package main.kotlin

import org.testng.Assert.assertEquals
import org.testng.Assert.assertThrows
import org.testng.annotations.Test
import java.lang.IllegalArgumentException
import java.util.*

// must add testng to the classpath
class AgeCalculation {
    fun getAge(dataBirth: Calendar): Int {
        val today = Calendar.getInstance()

        // we will throw an exception
        if (dataBirth.timeInMillis > today.timeInMillis) throw IllegalArgumentException()

        // we calculate the years
        val years = today.get(Calendar.YEAR) - dataBirth.get(Calendar.YEAR)

        // when day is the same, minus 1 year
        return if (dataBirth.get(Calendar.DAY_OF_YEAR) > today.get((Calendar.DAY_OF_YEAR)))
            years - 1
        else
            years
    }
}

// we will be using testng for this example
class AgeCalculationTests() {
    // test annotation
    @Test
    fun checkAgeWhenBornToday() {
        // test assertions
        assertEquals(0, AgeCalculation().getAge(Calendar.getInstance()))
    }

    @Test
    fun checkAgeWhenBorn1000DaysAgo() {
        val date = Calendar.getInstance()
        date.add(Calendar.DAY_OF_YEAR, -1000)
        assertEquals(2, AgeCalculation().getAge(date))
    }

    @Test
    fun testForException() {
        val date = Calendar.getInstance()
        date.add(Calendar.DAY_OF_YEAR, 10)

        // we are throwing the java illegal exception
        // we assert if the exception happens on the code block we specify
        assertThrows(IllegalArgumentException::class.java) { AgeCalculation().getAge(date) }
    }
}