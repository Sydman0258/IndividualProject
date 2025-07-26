package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.BookingModel
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

 class BookingRepositoryImpl : BookingRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("bookings")
    private val carRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("cars")
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
                if (newBooking.carId.isNotEmpty()) {
                    // Update car availability to false in Firebase
                    val carRef = FirebaseDatabase.getInstance().getReference("cars").child(newBooking.carId)
                    carRef.child("available").setValue(false)
                        .addOnSuccessListener {
                            callback(true, "Booking added and car marked unavailable!", bookingId)
                        }
                        .addOnFailureListener { e ->
                            callback(false, "Booking added but failed to update car availability: ${e.message}", bookingId)
                        }
                } else {
                    callback(true, "Booking added successfully! Car ID missing, availability not updated.", bookingId)
                }
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

    override fun updateCarAvailability(carName: String, available: Boolean, callback: (Boolean, String?) -> Unit) {
        val query = carRef.orderByChild("name").equalTo(carName)
        query.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                snapshot.children.forEach { carSnap ->
                    carSnap.ref.child("isAvailable").setValue(available)
                }
                callback(true, null)
            } else {
                callback(false, "Car not found.")
            }
        }.addOnFailureListener {
            callback(false, it.message)
        }
    }

}
