package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepositoryImpl : UserRepository {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val ref: DatabaseReference = database.reference.child("users")

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    callback(true, "Login successfull")
                } else {
                    callback(false, "${res.exception?.message}")
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
                    callback(
                        true, "Registration successfull",
                        "${auth.currentUser?.uid}"
                    )
                } else {
                    callback(
                        false,
                        "${res.exception?.message}", ""
                    )
                }

            }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "User successfully added")
            } else {
                callback(true, "${it.exception?.message}")

            }
        }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    callback(true, "Reset email sent to $email")
                } else {
                    callback(false, "${res.exception?.message}")
                }

            }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logout successfully")
        } catch (e: Exception) {
            callback(true, "${e.message}")
        }
    }

    override fun getUserFromDatabase(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            callback(true, "User fetched", user)
                        } else {
                            callback(false, "User data is null", null)
                        }
                    } else {
                        callback(false, "User not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }


    override fun editProfile(
        userId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Profile updated ")
            } else {
                callback(true, "${it.exception?.message}")

            }
        }
    }

    override fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Account deleted successfully")
            } else {
                callback(true, "${it.exception?.message}")

            }
        }
    }

}