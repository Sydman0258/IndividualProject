package com.example.vroomtrack.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete // NEW: Import the Delete icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vroomtrack.Repository.BookingRepositoryImpl
import com.example.vroomtrack.Repository.UserRepositoryImpl
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.example.vroomtrack.ViewModel.UserProfileViewModel // Make sure this ViewModel is correctly defined
import com.example.vroomtrack.model.BookingModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VroomTrackTheme {
                val userProfileViewModel: UserProfileViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
                                @Suppress("UNCHECKED_CAST")
                                return UserProfileViewModel(
                                    UserRepositoryImpl(),
                                    BookingRepositoryImpl(),
                                    FirebaseAuth.getInstance()
                                ) as T
                            }
                            throw IllegalArgumentException("Unknown ViewModel class")
                        }
                    }
                )
                UserProfileScreen(
                    onBackClick = { finish() },
                    userProfileViewModel = userProfileViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    userProfileViewModel: UserProfileViewModel
) {
    val context = LocalContext.current
    val uiState by userProfileViewModel.uiState.collectAsState()

    // Show Toast for general errors
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            userProfileViewModel.resetErrorState() // Clear the error after showing
        }
    }

    // NEW: Show Toast for delete success/failure
    LaunchedEffect(uiState.isDeleteSuccessful, uiState.deletedBookingId) {
        if (uiState.isDeleteSuccessful) {
            Toast.makeText(context, "Booking ${uiState.deletedBookingId?.takeLast(6)} deleted successfully!", Toast.LENGTH_SHORT).show()
            // No need to reset error state here, as it's a success state
        } else if (uiState.errorMessage != null && uiState.errorMessage!!.contains("Failed to delete booking")) {
            // This specific check ensures we only show delete-related errors here
            Toast.makeText(context, "Error deleting booking: ${uiState.errorMessage}", Toast.LENGTH_LONG).show()
        }
        // It's good practice to reset these specific delete flags if they exist in your UiState
        // You might add a resetDeleteState() to your ViewModel if you have more complex logic
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black // Set scaffold background to black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp)
                .background(Color.Black)
        ) {
            if (uiState.isLoading) {
                // Show a loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                // User Information Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)) // Dark grey card
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "User Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        uiState.user?.let { user ->
                            Text(
                                text = "Username: ${user.username}",
                                color = Color.LightGray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Email: ${user.email}",
                                color = Color.LightGray,
                                fontSize = 16.sp
                            )

                        } ?: run {
                            Text(
                                text = "User data not available.",
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Booking History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (uiState.bookings.isEmpty()) {
                    Text(
                        text = "No booking history found.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(top = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.bookings) { booking ->
                            BookingHistoryCard(
                                booking = booking,
                                // NEW: Pass the delete lambda
                                onDeleteClick = { bookingId ->
                                    userProfileViewModel.deleteBooking(bookingId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingHistoryCard(booking: BookingModel, onDeleteClick: (String) -> Unit) { // NEW: Add onDeleteClick parameter
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row( // NEW: Add Row to contain title and delete button
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${booking.carBrand} ${booking.carName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f) // Allow text to take most space
                )
                IconButton(
                    onClick = { onDeleteClick(booking.id) }, // NEW: Call delete lambda
                    modifier = Modifier.size(24.dp) // Smaller touch target
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete, // Delete icon
                        contentDescription = "Delete Booking",
                        tint = Color.Red // Red color for delete
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Booking ID: ${booking.id.takeLast(6)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

            booking.startDate?.let {
                Text(
                    text = "From: ${dateFormatter.format(it)}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
            booking.endDate?.let {
                Text(
                    text = "To: ${dateFormatter.format(it)}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total Cost: $${String.format("%.2f", booking.totalCost)}",
                color = Color.Green,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${booking.status}",
                color = when (booking.status) {
                    "Pending" -> Color(0xFFFFA000) // Orange
                    "Confirmed" -> Color(0xFF00C853) // Green
                    "Cancelled" -> Color(0xFFFF5252) // Red
                    "Completed" -> Color(0xFF2196F3) // Blue
                    else -> Color.LightGray
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Booked On: ${dateFormatter.format(booking.bookingDate)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}