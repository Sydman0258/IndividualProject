package com.example.vroomtrack



import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Car(

    val name: String,
    val brand: String,
    val imageRes: Int,
    val pricePerDay: String,
    val rating: Double,
    val description: String = "No description available." // Add a description field
) : Parcelable