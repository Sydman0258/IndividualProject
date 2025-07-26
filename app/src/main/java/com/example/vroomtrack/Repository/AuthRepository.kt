package com.example.vroomtrack.Repository

interface AuthRepository {
    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    )


    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit
    )
}