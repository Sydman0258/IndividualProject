package com.example.vroomtrack.model

//  Data class representing a Car entity used throughout the app (e.g., listings, bookings)
data class CarModel(
    val id: String = "", //  Unique identifier for the car (typically from Firebase or UUID)

    val name: String = "", //  Car name (e.g., Toyota Corolla)

    val brand: String = "", //  Car brand or manufacturer (e.g., Toyota)

    val imageUrl: String = "", //  Cloudinary/Firebase image URL for display

    val pricePerDay: String = "", //  Consider changing to Double for easier calculations
    //      Storing price as String may require conversion during operations (e.g., total cost)

    val rating: Double = 0.0, //  Admin rating (out of 5) used in car reviews or popularity sorting

    val description: String = "", //  Description shown in car details (can include mileage, features)

    val available: Boolean = true, // Indicates if car is currently available for rent

    val createdAt: Long = System.currentTimeMillis(), //  Timestamp for record creation (used for sorting or logs)

    val updatedAt: Long = System.currentTimeMillis()  //  Timestamp for last update (for audit or admin)
)
