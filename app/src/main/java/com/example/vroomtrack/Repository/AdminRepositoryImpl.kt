package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.AdminModel
import com.example.vroomtrack.model.CarModel
import com.example.vroomtrack.model.BookingModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminRepositoryImpl : AdminRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val carsRef = database.reference.child("cars")
    private val bookingsRef = database.reference.child("bookings")
    private val usersRef = database.reference.child("users")
    private val adminsRef = database.reference.child("admins")
    
    override fun adminLogin(email: String, password: String, callback: (Boolean, String) -> Unit) {
        // Check if admin credentials exist in database first
        adminsRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Admin exists, proceed with Firebase Auth
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback(true, "Admin login successful")
                                } else {
                                    callback(false, task.exception?.message ?: "Login failed")
                                }
                            }
                    } else {
                        callback(false, "Admin not found")
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }
    
    override fun addCar(car: CarModel, callback: (Boolean, String) -> Unit) {
        val carId = carsRef.push().key ?: return callback(false, "Failed to generate car ID")
        val carWithId = car.copy(id = carId)
        
        carsRef.child(carId).setValue(carWithId)
            .addOnSuccessListener { callback(true, "Car added successfully") }
            .addOnFailureListener { callback(false, it.message ?: "Failed to add car") }
    }
    
    override fun updateCar(car: CarModel, callback: (Boolean, String) -> Unit) {
        val updatedCar = car.copy(updatedAt = System.currentTimeMillis())
        carsRef.child(car.id).setValue(updatedCar)
            .addOnSuccessListener { callback(true, "Car updated successfully") }
            .addOnFailureListener { callback(false, it.message ?: "Failed to update car") }
    }
    
    override fun deleteCar(carId: String, callback: (Boolean, String) -> Unit) {
        carsRef.child(carId).removeValue()
            .addOnSuccessListener { callback(true, "Car deleted successfully") }
            .addOnFailureListener { callback(false, it.message ?: "Failed to delete car") }
    }
    
    override fun getAllCars(callback: (List<CarModel>) -> Unit) {
        carsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cars = mutableListOf<CarModel>()
                for (carSnapshot in snapshot.children) {
                    carSnapshot.getValue(CarModel::class.java)?.let { cars.add(it) }
                }
                callback(cars)
            }
            
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
    
    override fun getAllBookings(callback: (List<BookingModel>) -> Unit) {
        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookings = mutableListOf<BookingModel>()
                for (bookingSnapshot in snapshot.children) {
                    bookingSnapshot.getValue(BookingModel::class.java)?.let { bookings.add(it) }
                }
                callback(bookings)
            }
            
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
    
    override fun updateBookingStatus(bookingId: String, status: String, callback: (Boolean, String) -> Unit) {
        bookingsRef.child(bookingId).child("status").setValue(status)
            .addOnSuccessListener { callback(true, "Booking status updated") }
            .addOnFailureListener { callback(false, it.message ?: "Failed to update booking") }
    }
    
    override fun getUserCount(callback: (Int) -> Unit) {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.childrenCount.toInt())
            }
            
            override fun onCancelled(error: DatabaseError) {
                callback(0)
            }
        })
    }
    
    override fun getRevenueData(callback: (Double) -> Unit) {
        bookingsRef.orderByChild("status").equalTo("completed")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalRevenue = 0.0
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(BookingModel::class.java)
                        booking?.let { totalRevenue += it.totalCost }
                    }
                    callback(totalRevenue)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(0.0)
                }
            })
    }
}