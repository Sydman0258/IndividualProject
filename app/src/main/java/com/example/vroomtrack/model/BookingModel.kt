package com.example.vroomtrack.model

import java.util.Date

data class BookingModel(
    var id: String = "",
    val userId: String = "",
    val username: String = "",
    val carName: String = "",
    val carBrand: String = "",
    val carPricePerDay: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val totalCost: Double = 0.0,
    val bookingDate: Date = Date(),

    val status: String = "Pending"
)