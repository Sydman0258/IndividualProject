package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.AdminModel
import com.example.vroomtrack.model.CarModel
import com.example.vroomtrack.model.BookingModel

interface AdminRepository {
    fun adminLogin(email: String, password: String, callback: (Boolean, String) -> Unit)
    
    fun addCar(car: CarModel, callback: (Boolean, String) -> Unit)
    fun updateCar(car: CarModel, callback: (Boolean, String) -> Unit)
    fun deleteCar(carId: String, callback: (Boolean, String) -> Unit)
    fun getAllCars(callback: (List<CarModel>) -> Unit)
    
    fun getAllBookings(callback: (List<BookingModel>) -> Unit)
    fun updateBookingStatus(bookingId: String, status: String, callback: (Boolean, String) -> Unit)
    
    fun getUserCount(callback: (Int) -> Unit)
    fun getRevenueData(callback: (Double) -> Unit)
}