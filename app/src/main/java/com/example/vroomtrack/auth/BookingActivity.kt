package com.example.vroomtrack.auth

import android.content.Intent
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
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.example.vroomtrack.model.BookingModel
import com.example.vroomtrack.Repository.BookingRepositoryImpl
import com.example.vroomtrack.Repository.UserRepositoryImpl
import com.example.vroomtrack.ViewModel.BookingViewModel

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.Date

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider

import com.google.firebase.auth.FirebaseAuth

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

        if (selectedCar.imageRes == 0 && carName == "Unknown Car") {
            Toast.makeText(this, "Car details incomplete or not found!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            VroomTrackTheme {
                val bookingViewModel: BookingViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
                                @Suppress("UNCHECKED_CAST")
                                return BookingViewModel(
                                    BookingRepositoryImpl(),
                                    UserRepositoryImpl(),
                                    FirebaseAuth.getInstance()
                                ) as T
                            }
                            throw IllegalArgumentException("Unknown ViewModel class")
                        }
                    }
                )

                BookingScreen(
                    car = selectedCar,
                    onBackClick = { finish() },
                    bookingViewModel = bookingViewModel,
                    onBookingConfirmedAndNavigateToProfile = {
                        val intent = Intent(this@BookingActivity, UserProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
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
    bookingViewModel: BookingViewModel,
    onBookingConfirmedAndNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current

    val uiState by bookingViewModel.uiState.collectAsState()

    var startDate by remember { mutableStateOf<Calendar?>(null) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // States for Payment Information
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") } // MM/YY
    var cvv by remember { mutableStateOf("") }

    val bookingDurationDays = remember(startDate, endDate) {
        val startCal = startDate
        val endCal = endDate

        if (startCal != null && endCal != null && endCal.timeInMillis >= startCal.timeInMillis) {
            TimeUnit.MILLISECONDS.toDays(endCal.timeInMillis - startCal.timeInMillis) + 1
        } else {
            0L
        }
    }

    val dailyRateValue = remember(car.pricePerDay) {
        car.pricePerDay.replace("$", "").replace("/day", "").trim().toDoubleOrNull() ?: 0.0
    }

    val totalCost = remember(bookingDurationDays, dailyRateValue) {
        bookingDurationDays * dailyRateValue
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(uiState) {
        if (uiState.isBookingSuccessful) {
            Toast.makeText(context, "Booking Confirmed! ID: ${uiState.bookingId}", Toast.LENGTH_LONG).show()
            bookingViewModel.resetBookingState()
            onBookingConfirmedAndNavigateToProfile()
        } else if (uiState.errorMessage != null) {
            Toast.makeText(context, "Error: ${uiState.errorMessage}", Toast.LENGTH_LONG).show()
            bookingViewModel.resetBookingState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.    ArrowBack,
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

        Text(
            text = "Select Booking Dates:",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

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
                        showStartDatePicker.value = true
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
                        showEndDatePicker.value = true
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
        Text(
            text = "Payment Information:",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { if (it.length <= 19) cardNumber = it.filter { char -> char.isDigit() } },
            label = { Text("Card Number 13-19", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = Color.LightGray, unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = Color.LightGray, unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = expiryDate,
                onValueChange = {
                    if (it.length <= 5) {
                        val filtered = it.filter { char -> char.isDigit() || char == '/' }
                        expiryDate = when {
                            filtered.length == 2 && expiryDate.length == 1 && !filtered.contains('/') -> "$filtered/"
                            filtered.length == 2 && expiryDate.length == 3 && filtered.contains('/') -> filtered.substring(0,1)
                            else -> filtered
                        }
                    }
                },
                label = { Text("Expiry (MM/YY)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.LightGray, unfocusedBorderColor = Color.DarkGray,
                    focusedLabelColor = Color.LightGray, unfocusedLabelColor = Color.Gray,
                    cursorColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = cvv,
                onValueChange = { if (it.length <= 4) cvv = it.filter { char -> char.isDigit() } }, // Max 4 digits
                label = { Text("CVV", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.LightGray, unfocusedBorderColor = Color.DarkGray,
                    focusedLabelColor = Color.LightGray, unfocusedLabelColor = Color.Gray,
                    cursorColor = Color.White
                ),
                modifier = Modifier.weight(0.5f)
            )
        }


        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val currentStartDate = startDate
                val currentEndDate = endDate

                if (currentStartDate != null && currentEndDate != null && bookingDurationDays > 0) {
                    val booking = BookingModel(
                        carName = car.name,
                        carBrand = car.brand,
                        carPricePerDay = car.pricePerDay,
                        startDate = currentStartDate.time,
                        endDate = currentEndDate.time,
                        totalCost = totalCost,
                        bookingDate = Date(),
                        status = "Pending"
                    )
                    bookingViewModel.processBookingWithPayment(
                        booking,
                        cardNumber,
                        expiryDate,
                        cvv
                    )
                } else {
                    Toast.makeText(context, "Please select valid booking dates.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm Booking & Pay", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

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