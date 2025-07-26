package com.example.vroomtrack

import com.example.vroomtrack.model.BookingModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import java.text.SimpleDateFormat


class BookingUnitTest {

    private val sdf = SimpleDateFormat("yyyy-MM-dd")

    @Test
    fun testBookingModelInitialization() {
        val start = sdf.parse("2025-08-01")
        val end = sdf.parse("2025-08-05")

        val booking = BookingModel(
            id = "b1",
            userId = "u123",
            username = "Siddhartha",
            carId = "c456",
            carName = "Civic",
            carBrand = "Honda",
            carPricePerDay = "1000",
            startDate = start,
            endDate = end,
            totalCost = 4000.0,
            status = "Confirmed"
        )

        assertEquals("b1", booking.id)
        assertEquals("u123", booking.userId)
        assertEquals("Siddhartha", booking.username)
        assertEquals("c456", booking.carId)
        assertEquals("Civic", booking.carName)
        assertEquals("Honda", booking.carBrand)
        assertEquals("1000", booking.carPricePerDay)
        assertEquals(4000.0, booking.totalCost)
        assertEquals("Confirmed", booking.status)
        assertEquals(start, booking.startDate)
        assertEquals(end, booking.endDate)
        assertNotNull(booking.bookingDate)
    }

    @Test
    fun testBookingModelDefaultValues() {
        val booking = BookingModel()

        assertEquals("", booking.id)
        assertEquals("", booking.userId)
        assertEquals("", booking.username)
        assertEquals("", booking.carId)
        assertEquals("", booking.carName)
        assertEquals("", booking.carBrand)
        assertEquals("", booking.carPricePerDay)
        assertEquals(0.0, booking.totalCost)
        assertEquals("Pending", booking.status)
        assertNotNull(booking.bookingDate)
    }

    @Test
    fun testBookingDurationInDays() {
        val start = sdf.parse("2025-08-01")
        val end = sdf.parse("2025-08-06") // 5 days

        val days = (end.time - start.time) / (1000 * 60 * 60 * 24)
        assertEquals(5, days)
    }

    @Test
    fun testTotalCostCalculation() {
        val start = sdf.parse("2025-08-01")
        val end = sdf.parse("2025-08-06") // 5 days

        val pricePerDay = 1000.0
        val days = (end.time - start.time) / (1000 * 60 * 60 * 24)
        val expectedCost = pricePerDay * days

        assertEquals(5000.0, expectedCost)
    }
}
