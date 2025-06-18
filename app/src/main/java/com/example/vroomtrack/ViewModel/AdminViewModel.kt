package com.example.vroomtrack.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vroomtrack.Repository.AdminRepository
import com.example.vroomtrack.Repository.AdminRepositoryImpl
import com.example.vroomtrack.model.CarModel
import com.example.vroomtrack.model.BookingModel

class AdminViewModel(
    private val repo: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {
    
    private val _cars = MutableLiveData<List<CarModel>>()
    val cars: LiveData<List<CarModel>> = _cars
    
    private val _bookings = MutableLiveData<List<BookingModel>>()
    val bookings: LiveData<List<BookingModel>> = _bookings
    
    private val _userCount = MutableLiveData<Int>()
    val userCount: LiveData<Int> = _userCount
    
    private val _revenue = MutableLiveData<Double>()
    val revenue: LiveData<Double> = _revenue
    
    fun adminLogin(email: String, password: String, callback: (Boolean, String) -> Unit) {
        repo.adminLogin(email, password, callback)
    }
    
    fun addCar(car: CarModel, callback: (Boolean, String) -> Unit) {
        repo.addCar(car, callback)
    }
    
    fun updateCar(car: CarModel, callback: (Boolean, String) -> Unit) {
        repo.updateCar(car, callback)
    }
    
    fun deleteCar(carId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteCar(carId, callback)
    }
    
    fun loadCars() {
        repo.getAllCars { cars ->
            _cars.postValue(cars)
        }
    }
    
    fun loadBookings() {
        repo.getAllBookings { bookings ->
            _bookings.postValue(bookings)
        }
    }
    
    fun updateBookingStatus(bookingId: String, status: String, callback: (Boolean, String) -> Unit) {
        repo.updateBookingStatus(bookingId, status, callback)
    }
    
    fun loadAnalytics() {
        repo.getUserCount { count ->
            _userCount.postValue(count)
        }
        
        repo.getRevenueData { revenue ->
            _revenue.postValue(revenue)
        }
    }
}