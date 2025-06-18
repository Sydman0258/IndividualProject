package com.example.vroomtrack.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vroomtrack.Repository.BookingRepository
import com.example.vroomtrack.Repository.UserRepository
import com.example.vroomtrack.model.BookingModel
import com.example.vroomtrack.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class to hold the UI state for the User Profile screen
data class UserProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: UserModel? = null,
    val bookings: List<BookingModel> = emptyList()
)

class UserProfileViewModel(
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile() // Load data when the ViewModel is created
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState(isLoading = true, user = _uiState.value.user, bookings = _uiState.value.bookings) // Preserve existing data while loading

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.value = UserProfileUiState(errorMessage = "User not logged in.", isLoading = false)
                return@launch
            }

            val userId = currentUser.uid

            var fetchedUser: UserModel? = null
            var fetchedBookings: List<BookingModel> = emptyList()
            var userError: String? = null
            var bookingError: String? = null

            // Fetch user details
            userRepository.getUserFromDatabase(userId) { success, message, userModel ->
                if (success && userModel != null) {
                    fetchedUser = userModel
                } else {
                    userError = message
                }
                // Update state after user data fetch
                _uiState.value = _uiState.value.copy(
                    user = fetchedUser,
                    errorMessage = userError,
                    isLoading = false // Temporarily set false, will be re-evaluated after bookings
                )
            }

            // Fetch booking history
            bookingRepository.getBookingsByUserId(userId) { success, message, bookingsList ->
                if (success) {
                    fetchedBookings = bookingsList.sortedByDescending { it.bookingDate } // Sort by most recent
                } else {
                    bookingError = message
                }
                // Update state after bookings data fetch
                val finalErrorMessage = if (userError != null) {
                    if (bookingError != null) "$userError\n$bookingError" else userError
                } else {
                    bookingError
                }

                _uiState.value = _uiState.value.copy(
                    bookings = fetchedBookings,
                    errorMessage = finalErrorMessage,
                    isLoading = false
                )
            }
        }
    }

    fun resetErrorState() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}