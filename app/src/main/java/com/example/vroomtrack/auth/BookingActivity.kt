package com.example.vroomtrack.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.R // Ensure your R file is correctly imported for drawables
import com.example.vroomtrack.ui.theme.VroomTrackTheme // Your app's theme
import com.example.vroomtrack.model.BookingModel // Your BookingModel data class
import com.example.vroomtrack.Repository.BookingRepositoryImpl // Your BookingRepositoryImpl
import com.example.vroomtrack.Repository.UserRepositoryImpl // Your UserRepositoryImpl
import com.example.vroomtrack.ViewModel.BookingViewModel // Your BookingViewModel
import com.example.vroomtrack.ViewModel.BookingUiState // Your BookingUiState data class

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.Date

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel // For viewModel() Composable function
import androidx.lifecycle.ViewModelProvider // For custom ViewModel factory

import com.google.firebase.auth.FirebaseAuth // Firebase Authentication

// Assuming Car data class is defined elsewhere, or you can place it here for quick testing:
// If Car.kt exists as a separate file, remove this definition.
data class Car(
    val name: String,
    val brand: String,
    val imageRes: Int,
    val pricePerDay: String,
    val rating: Double,
    val description: String
)

class BookingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve car details passed via Intent
        val carName = intent.getStringExtra("car_name") ?: "Unknown Car"
        val carBrand = intent.getStringExtra("car_brand") ?: "Unknown Brand"
        val carImageResId = intent.getIntExtra("car_image_res_id", 0)
        val carPricePerDay = intent.getStringExtra("car_price_per_day") ?: "$0/day"
        val carRating = intent.getDoubleExtra("car_rating", 0.0)
        val carDescription = intent.getStringExtra("car_description") ?: "No description available."

        val selectedCar = Car(
            name = carName,
            brand = carBrand,
            imageRes = carImageResId,
            pricePerDay = carPricePerDay,
            rating = carRating,
            description = carDescription
        )

        // Basic validation for received car data
        if (selectedCar.imageRes == 0 && carName == "Unknown Car") {
            Toast.makeText(this, "Car details incomplete or not found!", Toast.LENGTH_LONG).show()
            finish() // Close activity if essential data is missing
            return
        }

        setContent {
            VroomTrackTheme {
                // Instantiate BookingViewModel using a ViewModelProvider.Factory
                // This factory ensures that BookingViewModel receives its required dependencies.
                val bookingViewModel: BookingViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
                                @Suppress("UNCHECKED_CAST")
                                return BookingViewModel(
                                    BookingRepositoryImpl(), // Provides the BookingRepository
                                    UserRepositoryImpl(),    // Provides the UserRepository
                                    FirebaseAuth.getInstance() // Provides the FirebaseAuth instance
                                ) as T
                            }
                            throw IllegalArgumentException("Unknown ViewModel class")
                        }
                    }
                )

                // The main Composable screen for booking
                BookingScreen(
                    car = selectedCar,
                    onBackClick = { finish() }, // Lambda to close the activity
                    bookingViewModel = bookingViewModel // Pass the ViewModel to the Composable
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    car: Car,
    onBackClick: () -> Unit,
    bookingViewModel: BookingViewModel // The ViewModel instance
) {
    val context = LocalContext.current // Context for Toast messages

    // Observe the UI state from the ViewModel
    // This recomposes the UI whenever the uiState changes (e.g., loading, success, error)
    val uiState by bookingViewModel.uiState.collectAsState()

    // State for selected start and end dates
    var startDate by remember { mutableStateOf<Calendar?>(null) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }

    // State to control visibility of DatePickerDialogs
    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    // DatePickerState for Material3 DatePicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() // Default to current date
    )

    // Calculate booking duration in days
    val bookingDurationDays = remember(startDate, endDate) {
        val startCal = startDate
        val endCal = endDate

        if (startCal != null && endCal != null && endCal.timeInMillis >= startCal.timeInMillis) {
            // +1 to include both start and end day
            TimeUnit.MILLISECONDS.toDays(endCal.timeInMillis - startCal.timeInMillis) + 1
        } else {
            0L // Invalid duration
        }
    }

    // Parse daily rate from car string (e.g., "$50/day" -> 50.0)
    val dailyRateValue = remember(car.pricePerDay) {
        car.pricePerDay.replace("$", "").replace("/day", "").trim().toDoubleOrNull() ?: 0.0
    }

    // Calculate total cost
    val totalCost = remember(bookingDurationDays, dailyRateValue) {
        bookingDurationDays * dailyRateValue
    }

    // Scroll state for the column
    val scrollState = rememberScrollState()

    // LaunchedEffect to react to changes in uiState and show Toasts
    LaunchedEffect(uiState) {
        if (uiState.isBookingSuccessful) {
            Toast.makeText(context, "Booking Confirmed! ID: ${uiState.bookingId}", Toast.LENGTH_LONG).show()
            // Reset ViewModel state after success to allow new bookings or clear UI
            bookingViewModel.resetBookingState()
            // Optionally navigate back after successful booking:
            // onBackClick()
        } else if (uiState.errorMessage != null) {
            Toast.makeText(context, "Error: ${uiState.errorMessage}", Toast.LENGTH_LONG).show()
            // Reset error state to allow user to retry
            bookingViewModel.resetBookingState()
        }
    }

    // Main layout column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState) // Make content scrollable
            .padding(16.dp)
    ) {
        // Top bar with back button and title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Book ${car.name}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Car Image
        if (car.imageRes != 0) {
            Image(
                painter = painterResource(id = car.imageRes),
                contentDescription = car.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder if image resource is not found
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Image Not Available", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Car Details
        Text(
            text = car.name,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = car.brand,
            color = Color.Gray,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = car.description,
            color = Color.LightGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Justify
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Daily Rate: ${car.pricePerDay}",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Rating: ${car.rating} / 5.0",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Date Selection Section
        Text(
            text = "Select Booking Dates:",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Start Date TextField
        OutlinedTextField(
            value = startDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.time) } ?: "Select Start Date",
            onValueChange = { /* Read-only */ },
            label = { Text("Start Date", color = Color.Gray) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Start Date",
                    tint = Color.White,
                    modifier = Modifier.clickable {
                        showStartDatePicker.value = true // Show start date picker on click
                    }
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White,
                disabledTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // End Date TextField
        OutlinedTextField(
            value = endDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.time) } ?: "Select End Date",
            onValueChange = { /* Read-only */ },
            label = { Text("End Date", color = Color.Gray) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select End Date",
                    tint = Color.White,
                    modifier = Modifier.clickable {
                        showEndDatePicker.value = true // Show end date picker on click
                    }
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White,
                disabledTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Duration and Cost display
        if (startDate != null && endDate != null && bookingDurationDays > 0) {
            Text(
                text = "Duration: $bookingDurationDays day(s)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Estimated Total: $${String.format("%.2f", totalCost)}",
                color = Color.Green,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (startDate != null || endDate != null) {
            Text(
                text = "Please select both start and end dates, with end date on or after start date.",
                color = Color.Red,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm Booking Button
        Button(
            onClick = {
                val currentStartDate = startDate
                val currentEndDate = endDate

                if (currentStartDate != null && currentEndDate != null && bookingDurationDays > 0) {
                    // Create BookingModel instance. userId and username will be filled by ViewModel.
                    val booking = BookingModel(
                        userId = "", // Will be populated by ViewModel
                        username = "", // Will be populated by ViewModel
                        carName = car.name,
                        carBrand = car.brand,
                        carPricePerDay = car.pricePerDay,
                        startDate = currentStartDate.time,
                        endDate = currentEndDate.time,
                        totalCost = totalCost,
                        bookingDate = Date(), // Current date of booking
                        status = "Pending" // Initial status
                    )
                    bookingViewModel.addBooking(booking) // Trigger booking process in ViewModel
                } else {
                    Toast.makeText(context, "Please select valid booking dates.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading, // Disable button when loading
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)) // Green color
        ) {
            if (uiState.isLoading) {
                // Show loading indicator when booking is in progress
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm Booking", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Start DatePickerDialog
    if (showStartDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Calendar.getInstance().apply { timeInMillis = millis }
                    }
                    showStartDatePicker.value = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker.value = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End DatePickerDialog
    if (showEndDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        endDate = Calendar.getInstance().apply { timeInMillis = millis }
                    }
                    showEndDatePicker.value = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker.value = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}