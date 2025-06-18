package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.BookingModel

interface BookingRepository {

    fun addBooking(
        booking: BookingModel,
        callback: (Boolean, String, String?) -> Unit
    )

    fun getBookingById(
        bookingId: String,
        callback: (Boolean, String, BookingModel?) -> Unit
    )

    fun getBookingsForUser(
        userId: String,
        callback: (Boolean, String, List<BookingModel>?) -> Unit
    )

    fun updateBooking(
        bookingId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    )

    fun deleteBooking(
        bookingId: String,
        callback: (Boolean, String) -> Unit
    )
}