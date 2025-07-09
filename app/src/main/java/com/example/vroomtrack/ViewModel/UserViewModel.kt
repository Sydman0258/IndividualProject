package com.example.vroomtrack.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vroomtrack.Repository.UserRepository
import com.example.vroomtrack.model.UserModel
import com.google.firebase.auth.FirebaseUser

class UserViewModel(val repo : UserRepository) : ViewModel() {

    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.login(email,password,callback)
    }

    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        repo.register(email,password,callback)
    }

    fun addUserToDatabase(
        userId: String, model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addUserToDatabase(userId,model,callback)
    }

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgetPassword(email,callback)
    }

    fun getCurrentUser(): FirebaseUser? {
        return repo.getCurrentUser()
    }

    fun getCurrentUserUid(): String? {
        return getCurrentUser()?.uid
    }

    private val _users = MutableLiveData<UserModel?>()
    val users : LiveData<UserModel?> get() = _users

    // Modified to directly return user model in callback for easy UI logic
    fun getUserFromDatabase(
        userId: String,
        callback: (UserModel?, String?) -> Unit
    ) {
        repo.getUserFromDatabase(userId) { success, message, userModel ->
            if (success) {
                _users.postValue(userModel)
                callback(userModel, null)
            } else {
                _users.postValue(null)
                callback(null, message)
            }
        }
    }


    fun logout(callback: (Boolean, String) -> Unit) {
        repo.logout(callback)
    }

    fun editProfile(
        userId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        repo.editProfile(userId,data,callback)
    }

    fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteAccount(userId,callback)
    }
}
