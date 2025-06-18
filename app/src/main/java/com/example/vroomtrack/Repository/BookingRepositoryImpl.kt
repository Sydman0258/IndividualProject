package com.example.vroomtrack.Repository

import android.util.Log
import com.example.vroomtrack.model.BookingModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot // Import for Realtime DB

class BookingRepositoryImpl : BookingRepository {

    private val database = FirebaseDatabase.getInstance()
    private val bookingsRef = database.getReference("bookings") // Reference to the "bookings" node

    companion object {
        private const val TAG = "BookingRepoImplRTDB" // Tag for Logcat
    }

    override fun addBooking(
        booking: BookingModel,
        callback: (Boolean, String, String?) -> Unit
    ) {
        Log.d(TAG, "Attempting to add booking to Realtime DB for car: ${booking.carName}, User ID: ${booking.userId}")

        // Use push() to generate a unique key
        val newBookingRef = bookingsRef.push()
        val generatedId = newBookingRef.key

        if (generatedId == null) {
            callback(false, "Failed to generate unique key for booking.", null)
            Log.e(TAG, "Failed to generate unique key.")
            return
        }

        // Set the generated ID into the booking object before saving
        booking.id = generatedId

        newBookingRef.setValue(booking)
            .addOnSuccessListener {
                Log.d(TAG, "Booking added successfully to Realtime DB with ID: $generatedId")
                callback(true, "Booking added successfully!", generatedId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add booking to Realtime DB: ${e.message}", e)
                callback(false, "Failed to add booking: ${e.message}", null)
            }
    }

    override fun getBookingById(
        bookingId: String,
        callback: (Boolean, String, BookingModel?) -> Unit
    ) {
        bookingsRef.child(bookingId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val booking = snapshot.getValue(BookingModel::class.java)
                    if (booking != null) {
                        booking.id = snapshot.key ?: bookingId // Ensure ID is set from the key
                        callback(true, "Booking retrieved successfully!", booking)
                    } else {
                        callback(false, "Failed to parse booking data.", null)
                    }
                } else {
                    callback(false, "Booking not found.", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Failed to retrieve booking: ${error.message}", null)
            }
        })
    }

    override fun getBookingsForUser(
        userId: String,
        callback: (Boolean, String, List<BookingModel>?) -> Unit
    ) {
        // Realtime Database querying is slightly different.
        // We query by the "userId" child.
        bookingsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookings = mutableListOf<BookingModel>()
                    for (childSnapshot in snapshot.children) {
                        val booking = childSnapshot.getValue(BookingModel::class.java)
                        if (booking != null) {
                            booking.id = childSnapshot.key ?: booking.id // Set ID from the key
                            bookings.add(booking)
                        }
                    }
                    callback(true, "Bookings retrieved successfully!", bookings)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Failed to retrieve bookings for user: ${error.message}", null)
                }
            })
    }

    override fun updateBooking(
        bookingId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        bookingsRef.child(bookingId).updateChildren(data)
            .addOnSuccessListener {
                callback(true, "Booking updated successfully!")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update booking: ${e.message}")
            }
    }

    override fun deleteBooking(
        bookingId: String,
        callback: (Boolean, String) -> Unit
    ) {
        bookingsRef.child(bookingId).removeValue()
            .addOnSuccessListener {
                callback(true, "Booking deleted successfully!")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to delete booking: ${e.message}")
            }
    }
}