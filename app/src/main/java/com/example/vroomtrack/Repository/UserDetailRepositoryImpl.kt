package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.UserDetailModel
import com.google.firebase.database.FirebaseDatabase

class UserDetailRepositoryImpl : UserDetailRepository {

    private val database = FirebaseDatabase.getInstance().getReference("user_details")

    override fun saveUserDetails(user: UserDetailModel, onResult: (Boolean) -> Unit) {
        database.child(user.userId).setValue(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    override fun getUserDetails(userId: String, onResult: (UserDetailModel?) -> Unit) {
        database.child(userId).get()
            .addOnSuccessListener {
                onResult(it.getValue(UserDetailModel::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

}
