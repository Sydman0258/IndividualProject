package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.BookingModel

interface BookingRepository {
    fun addBooking(booking: BookingModel, callback: (Boolean, String, String?) -> Unit)
    // Add this new function:
    fun getBookingsByUserId(userId: String, callback: (Boolean, String, List<BookingModel>) -> Unit)
    // You might also want methods to update/cancel bookings, etc.
}