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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.vroomtrack.LoginActivity
import com.example.vroomtrack.Repository.UserRepositoryImpl
import com.example.vroomtrack.ViewModel.CarViewModel
import com.example.vroomtrack.ViewModel.UserViewModel
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VroomTrackTheme {
                DashboardScreen()

        }
    }
}

@Composable
fun DashboardScreen(
    carViewModel: CarViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return UserViewModel(UserRepositoryImpl()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
) {
    val context = LocalContext.current

    val firebaseUser = userViewModel.getCurrentUser()
    val userData by userViewModel.users.observeAsState()
    var searchQuery by remember { mutableStateOf("") }
    val cars by carViewModel.cars.collectAsState()

    // Load user data
    LaunchedEffect(Unit) {
        firebaseUser?.uid?.let { uid ->
            userViewModel.getUserFromDatabase(uid) { userModel, errorMsg ->
                if (userModel == null) {
                    Toast.makeText(context, errorMsg ?: "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val filteredCars = cars.filter {
        it.available && it.name.contains(searchQuery, ignoreCase = true)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header with user profile, settings, and logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, UserProfileActivity::class.java))
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = userData?.username ?: "Loading...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row {
                IconButton(onClick = {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }

                IconButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    if (context is ComponentActivity) {
                        context.finish()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Search cars...", color = Color.Gray)
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Available Cars", color = Color.White, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(filteredCars) { car ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color(0xFF1C1C1E))
                ) {
                    AsyncImage(
                        model = car.imageUrl,
                        contentDescription = "Car Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable {
                                val intent = Intent(context, BookingActivity::class.java).apply {
                                    putExtra("car_id", car.id)
                                    putExtra("car_name", car.name)
                                    putExtra("car_brand", car.brand)
                                    putExtra("car_image_url", car.imageUrl)
                                    putExtra("car_price_per_day", car.pricePerDay)
                                    putExtra("car_rating", car.rating)
                                    putExtra("car_description", car.description)
                                }
                                context.startActivity(intent)
                            },
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = car.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = car.brand,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$" + car.pricePerDay,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Rating: ${car.rating} / 5.0",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Text(
            text = "See More",
            color = Color(0xFF1E88E5),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable {
                    val intent = Intent(context, AllCarActivity::class.java)
                    context.startActivity(intent)
                }
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }   }
}
