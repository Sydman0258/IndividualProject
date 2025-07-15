package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.BookingModel

interface BookingRepository {
    fun addBooking(booking: BookingModel, callback: (Boolean, String?, String?) -> Unit)
    fun getBookingsForUser(userId: String, callback: (List<BookingModel>?, String?) -> Unit)
    fun deleteBooking(bookingId: String, callback: (Boolean, String?) -> Unit)
    fun updateCarAvailability(carName: String, available: Boolean, callback: (Boolean, String?) -> Unit)

}