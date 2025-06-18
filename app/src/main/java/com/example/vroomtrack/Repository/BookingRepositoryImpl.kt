package com.example.vroomtrack.Repository

import android.util.Log
import com.example.vroomtrack.model.BookingModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BookingRepositoryImpl : BookingRepository {

    private val database = FirebaseDatabase.getInstance()
    private val bookingsRef = database.getReference("bookings")

    companion object {
        private const val TAG = "BookingRepoImpl"
    }

    override fun addBooking(booking: BookingModel, callback: (Boolean, String, String?) -> Unit) {
        val newBookingRef = bookingsRef.push()
        val bookingId = newBookingRef.key ?: ""

        val bookingWithId = booking.copy(id = bookingId)

        newBookingRef.setValue(bookingWithId)
            .addOnSuccessListener {
                Log.d(TAG, "Booking added successfully with ID: $bookingId")
                callback(true, "Booking added successfully!", bookingId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add booking: ${e.message}", e)
                callback(false, "Failed to add booking: ${e.message}", null)
            }
    }

    override fun getBookingsByUserId(
        userId: String,
        callback: (Boolean, String, List<BookingModel>) -> Unit
    ) {
        bookingsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookings = mutableListOf<BookingModel>()
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(BookingModel::class.java)
                        booking?.let {
                            bookings.add(it)
                        }
                    }
                    Log.d(TAG, "Fetched ${bookings.size} bookings for user $userId")
                    callback(true, "Bookings retrieved successfully!", bookings)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to retrieve bookings: ${error.message}", error.toException())
                    callback(false, "Failed to retrieve bookings: ${error.message}", emptyList())
                }
            })
    }
}