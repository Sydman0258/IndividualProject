package com.example.vroomtrack.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vroomtrack.Repository.BookingRepository
import com.example.vroomtrack.Repository.UserRepository
import com.example.vroomtrack.model.BookingModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BookingUiState(
    val isLoading: Boolean = false,
    val isBookingSuccessful: Boolean = false,
    val bookingId: String? = null,
    val errorMessage: String? = null,
    val isDeleteSuccessful: Boolean = false,
    val deletedBookingId: String? = null
)

class BookingViewModel(
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _userBookings = MutableStateFlow<List<BookingModel>>(emptyList())
    val userBookings: StateFlow<List<BookingModel>> = _userBookings.asStateFlow()

    init {
        fetchUserBookings()
    }

    fun fetchUserBookings() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "User not logged in.")
            _userBookings.value = emptyList()
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        bookingRepository.getBookingsForUser(currentUser.uid) { bookings, errorMessage ->
            if (bookings != null) {
                _userBookings.value = bookings
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
            } else {
                _userBookings.value = emptyList()
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage ?: "Failed to load bookings.")
            }
        }
    }

    fun processBookingWithPayment(
        booking: BookingModel,
        cardNumber: String,
        expiryDate: String,
        cvv: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, isBookingSuccessful = false)

            if (!isValidCardNumber(cardNumber) || !isValidExpiry(expiryDate) || !isValidCvv(cvv)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid card details. Please check number, expiry (MM/YY), and CVV."
                )
                return@launch
            }

            delay(1500)

            val paymentSuccessful = true

            if (paymentSuccessful) {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not logged in. Cannot complete booking."
                    )
                    return@launch
                }

                val userId = currentUser.uid

                userRepository.getUserFromDatabase(userId) { success, message, userModel ->
                    if (success && userModel != null) {
                        val finalBooking = booking.copy(
                            userId = userId,
                            username = userModel.username,
                            status = "Confirmed"
                        )

                        bookingRepository.addBooking(finalBooking) { bookingSuccess, bookingMessage, bookingId ->
                            if (bookingSuccess) {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    isBookingSuccessful = true,
                                    bookingId = bookingId,
                                    errorMessage = null
                                )
                                fetchUserBookings()
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Booking failed after payment: $bookingMessage"
                                )
                            }
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to fetch user data for booking: $message"
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Payment failed. Please try again or use a different card."
                )
            }
        }
    }

    fun deleteBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
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
                    fetchUserBookings()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage ?: "Failed to delete booking.",
                        isDeleteSuccessful = false
                    )
                }
            }
        }
    }

    private fun isValidCardNumber(number: String): Boolean {
        return number.length in 13..19 && number.all { it.isDigit() }
    }

    private fun isValidExpiry(expiry: String): Boolean {
        if (!expiry.matches(Regex("^\\d{2}/\\d{2}$"))) return false
        val parts = expiry.split("/")
        val month = parts[0].toIntOrNull() ?: 0
        val year = parts[1].toIntOrNull() ?: 0

        if (month !in 1..12) return false

        val currentYearLastTwoDigits = SimpleDateFormat("yy", Locale.getDefault()).format(Date()).toInt()
        val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date()).toInt()

        if (year < currentYearLastTwoDigits) return false
        if (year == currentYearLastTwoDigits && month < currentMonth) return false

        return true
    }

    private fun isValidCvv(cvv: String): Boolean {
        return cvv.length in 3..4 && cvv.all { it.isDigit() }
    }

    fun resetBookingState() {
        _uiState.value = BookingUiState()
    }
}