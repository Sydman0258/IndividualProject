package com.example.vroomtrack.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vroomtrack.Repository.UserDetailRepository
import com.example.vroomtrack.Repository.UserDetailRepositoryImpl
import com.example.vroomtrack.model.UserDetailModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserDetailViewModel(
    private val repo: UserDetailRepository = UserDetailRepositoryImpl()
) : ViewModel() {

    // MutableStateFlow to hold user details, allowing UI to observe changes
    private val _userDetails = MutableStateFlow<UserDetailModel?>(null)
    val userDetails: StateFlow<UserDetailModel?> = _userDetails

    fun saveDetails(user: UserDetailModel, onResult: (Boolean) -> Unit) {
        repo.saveUserDetails(user, onResult)
    }

    // Function to fetch user details and update the StateFlow
    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            repo.getUserDetails(userId) { user ->
                _userDetails.value = user
            }
        }
    }
}