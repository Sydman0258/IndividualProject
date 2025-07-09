package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.CarModel // <-- Updated import
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    fun getAllCars(): Flow<List<CarModel>> // Get all cars, observing changes
    suspend fun getCarById(id: String): CarModel? // Get a single car by ID
    suspend fun addCar(car: CarModel): Boolean // Add a new car
    suspend fun updateCar(car: CarModel): Boolean // Update an existing car
    suspend fun deleteCar(id: String): Boolean // Delete a car by ID
}