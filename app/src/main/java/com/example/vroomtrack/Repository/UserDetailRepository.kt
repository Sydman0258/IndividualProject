package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.UserDetailModel

interface UserDetailRepository {
    fun saveUserDetails(user: UserDetailModel, onResult: (Boolean) -> Unit)
    fun getUserDetails(userId: String, onResult: (UserDetailModel?) -> Unit)
}
