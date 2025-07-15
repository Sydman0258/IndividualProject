package com.example.vroomtrack.model

data class CarModel(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val imageUrl: String = "",
    val pricePerDay: String = "",
    val rating: Double = 0.0,
    val description: String = "",
    val available: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)