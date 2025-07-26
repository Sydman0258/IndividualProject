package com.example.vroomtrack.Repository

import com.google.firebase.auth.FirebaseAuth

class AuthRepositoryImpl(val auth: FirebaseAuth): AuthRepository {
    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    callback(true, "Login successful")
                } else {
                    callback(false, res.exception?.message ?: "Login failed")
                }
            }
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    callback(true, "Registration successful", auth.currentUser?.uid ?: "")
                } else {
                    callback(false, res.exception?.message ?: "Registration failed", "")
                }
            }
    }

}