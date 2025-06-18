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

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val user: UserModel? = null,
    val bookings: List<BookingModel> = emptyList(),
    val errorMessage: String? = null,
    val isDeleteSuccessful: Boolean = false,
    val deletedBookingId: String? = null
)

class UserProfileViewModel(
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        fetchUserProfileData()
    }

    private fun fetchUserProfileData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "User not logged in."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            userRepository.getUserFromDatabase(currentUser.uid) { success, message, userModel ->
                if (success && userModel != null) {
                    _uiState.value = _uiState.value.copy(user = userModel)
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = message ?: "Failed to load user data.")
                }
                fetchUserBookings(currentUser.uid)
            }
        }
    }

    private fun fetchUserBookings(userId: String) {
        bookingRepository.getBookingsForUser(userId) { bookings, errorMessage ->
            if (bookings != null) {
                val sortedBookings = bookings.sortedByDescending { it.bookingDate }
                _uiState.value = _uiState.value.copy(
                    bookings = sortedBookings,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage ?: "Failed to load bookings.",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Deletes a booking from the database and refreshes the booking list.
     * @param bookingId The ID of the booking to delete.
     */
    fun deleteBooking(bookingId: String) { // <--- THIS IS THE FUNCTION
        _uiState.value = _uiState.value.copy(
            isLoading = true, // Optionally show loading during delete
            errorMessage = null,
            isDeleteSuccessful = false,
            deletedBookingId = null
        )
        bookingRepository.deleteBooking(bookingId) { success, errorMessage ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isDeleteSuccessful = true,
                    deletedBookingId = bookingId,
                    errorMessage = null
                )
                // Refresh the list of bookings after successful deletion
                auth.currentUser?.uid?.let { userId ->
                    fetchUserBookings(userId)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage ?: "Failed to delete booking.",
                    isDeleteSuccessful = false
                )
            }
        }
    }


    fun resetErrorState() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }


    fun resetDeleteState() {
        _uiState.value = _uiState.value.copy(isDeleteSuccessful = false, deletedBookingId = null)
    }
}