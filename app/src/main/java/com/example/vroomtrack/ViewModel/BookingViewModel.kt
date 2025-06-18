package com.example.vroomtrack.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vroomtrack.Repository.BookingRepository
import com.example.vroomtrack.Repository.UserRepository
import com.example.vroomtrack.model.BookingModel
import com.example.vroomtrack.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth


data class BookingUiState(
    val isLoading: Boolean = false,
    val isBookingSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val bookingId: String? = null
)

class BookingViewModel(
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun addBooking(booking: BookingModel) {
        viewModelScope.launch {
            _uiState.value = BookingUiState(isLoading = true)

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.value = BookingUiState(errorMessage = "User not logged in. Please log in to book a car.")
                return@launch
            }

            val currentUserId = currentUser.uid

            userRepository.getUserFromDatabase(currentUserId) { success, message, userModel ->
                if (success && userModel != null) {
                    val usernameFromModel = userModel.username // <--- ACCESS 'username' (lowercase 'u')

                    val bookingWithUserAndId = booking.copy(
                        userId = currentUserId,
                        username = usernameFromModel // <--- USE 'username'
                    )

                    bookingRepository.addBooking(bookingWithUserAndId) { bookingSuccess, bookingMessage, bookingId ->
                        if (bookingSuccess) {
                            _uiState.value = BookingUiState(isBookingSuccessful = true, bookingId = bookingId)
                        } else {
                            _uiState.value = BookingUiState(errorMessage = bookingMessage)
                        }
                    }
                } else {
                    _uiState.value = BookingUiState(errorMessage = "Failed to get user details: $message")
                }
            }
        }
    }

    fun resetBookingState() {
        _uiState.value = BookingUiState()
    }
}