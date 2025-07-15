package com.example.vroomtrack.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.vroomtrack.Repository.BookingRepositoryImpl
import com.example.vroomtrack.Repository.UserRepositoryImpl
import com.example.vroomtrack.ViewModel.BookingViewModel
import com.example.vroomtrack.model.BookingModel
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class Car(
    val id : String,
    val name: String,
    val brand: String,
    val imageUrl: String,
    val pricePerDay: String,
    val rating: Double,
    val description: String
)

class BookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val carId = intent.getStringExtra("car_id") ?: ""
        val carName = intent.getStringExtra("car_name") ?: "Unknown Car"
        val carBrand = intent.getStringExtra("car_brand") ?: "Unknown Brand"
        val carImageUrl = intent.getStringExtra("car_image_url") ?: ""
        val carPricePerDay = intent.getStringExtra("car_price_per_day") ?: "$0/day"
        val carRating = intent.getDoubleExtra("car_rating", 0.0)
        val carDescription = intent.getStringExtra("car_description") ?: "No description available."

        val selectedCar = Car(
            id = carId,
            name = carName,
            brand = carBrand,
            imageUrl = carImageUrl,
            pricePerDay = carPricePerDay,
            rating = carRating,
            description = carDescription
        )

        if (selectedCar.imageUrl.isBlank() || carName == "Unknown Car") {
            Toast.makeText(this, "Car details incomplete or not found!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            VroomTrackTheme {
                val bookingViewModel: BookingViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
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

    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val bookingDurationDays = remember(startDate, endDate) {
        if (startDate != null && endDate != null && endDate!!.timeInMillis >= startDate!!.timeInMillis) {
            TimeUnit.MILLISECONDS.toDays(endDate!!.timeInMillis - startDate!!.timeInMillis) + 1
        } else 0
    }

    val dailyRateValue = car.pricePerDay.replace("$", "").replace("/day", "").trim().toDoubleOrNull() ?: 0.0
    val totalCost = bookingDurationDays * dailyRateValue

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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { onBackClick() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Book ${car.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = car.imageUrl,
            contentDescription = car.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = car.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = car.brand, color = Color.Gray, fontSize = 16.sp)
        Text(text = car.description, color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Justify)
        Text(text = "Daily Rate: ${car.pricePerDay}", color = Color.White, fontSize = 18.sp)
        Text(text = "Rating: ${car.rating} / 5.0", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select Booking Dates:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = startDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.time) }
                ?: "Select Start Date",
            onValueChange = {},
            label = { Text("Start Date", color = Color.Gray) },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White,
                    modifier = Modifier.clickable { showStartDatePicker.value = true })
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = endDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.time) }
                ?: "Select End Date",
            onValueChange = {},
            label = { Text("End Date", color = Color.Gray) },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White,
                    modifier = Modifier.clickable { showEndDatePicker.value = true })
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (startDate != null && endDate != null && bookingDurationDays > 0) {
            Text("Duration: $bookingDurationDays day(s)", color = Color.White)
            Text("Estimated Total: $${String.format("%.2f", totalCost)}", color = Color.Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Payment Information", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { if (it.length <= 19) cardNumber = it.filter(Char::isDigit) },
            label = { Text("Card Number", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = expiryDate,
                onValueChange = { if (it.length <= 5) expiryDate = it },
                label = { Text("Expiry MM/YY", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = cvv,
                onValueChange = { if (it.length <= 4) cvv = it.filter(Char::isDigit) },
                label = { Text("CVV", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (startDate != null && endDate != null && bookingDurationDays > 0) {
                    val booking = BookingModel(
                        carName = car.name,
                        carBrand = car.brand,
                        carPricePerDay = car.pricePerDay,
                        startDate = startDate!!.time,
                        endDate = endDate!!.time,
                        totalCost = totalCost,
                        bookingDate = Date(),
                        status = "Pending"
                    )
                    bookingViewModel.processBookingWithPayment(booking, cardNumber, expiryDate, cvv)
                } else {
                    Toast.makeText(context, "Please select valid booking dates.", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
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
                    startDatePickerState.selectedDateMillis?.let {
                        startDate = Calendar.getInstance().apply { timeInMillis = it }
                    }
                    showStartDatePicker.value = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker.value = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        endDate = Calendar.getInstance().apply { timeInMillis = it }
                    }
                    showEndDatePicker.value = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker.value = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}
