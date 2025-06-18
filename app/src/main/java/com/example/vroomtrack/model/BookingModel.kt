package com.example.vroomtrack.model

data class BookingModel(
    val id: String = "",
    val userId: String = "",
    val carId: String = "",
    val carName: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val totalCost: Double = 0.0,
    val status: String = "confirmed", // confirmed, cancelled, completed
    val createdAt: Long = System.currentTimeMillis()
)