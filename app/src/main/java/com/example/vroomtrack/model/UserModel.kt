package com.example.vroomtrack.model


data class UserModel(
    val username: String = "",
    val email: String = "",
    val password: String = "",

    var admin: Boolean = false ,
)
