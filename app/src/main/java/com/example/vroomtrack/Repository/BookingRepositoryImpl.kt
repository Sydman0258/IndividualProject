package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.BookingModel
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

class BookingRepositoryImpl : BookingRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookings")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun addBooking(booking: BookingModel, callback: (Boolean, String?, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not logged in.", null)
            return
        }

        val bookingRef = database.push()
        val bookingId = bookingRef.key ?: return callback(false, "Could not generate booking ID.", null)

        val newBooking = booking.copy(id = bookingId)

        bookingRef.setValue(newBooking)
            .addOnSuccessListener {
                callback(true, "Booking added successfully!", bookingId)
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to add booking: ${e.message}", null)
            }
    }

    override fun getBookingsForUser(userId: String, callback: (List<BookingModel>?, String?) -> Unit) {
        database.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookings = mutableListOf<BookingModel>()
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(BookingModel::class.java)
                        booking?.let { bookings.add(it) }
                    }
                    callback(bookings, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null, error.message)
                }
            })
    }

    override fun deleteBooking(bookingId: String, callback: (Boolean, String?) -> Unit) {
        database.child(bookingId).removeValue()
            .addOnSuccessListener {
                callback(true, "Booking deleted successfully!")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to delete booking: ${e.message}")
            }
    }
}