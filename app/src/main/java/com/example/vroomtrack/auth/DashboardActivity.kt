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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.R
import com.example.vroomtrack.Repository.UserRepositoryImpl
import com.example.vroomtrack.ViewModel.UserViewModel
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.example.vroomtrack.Car
import coil.compose.AsyncImage
import com.example.vroomtrack.Repository.AdminRepositoryImpl
import com.example.vroomtrack.ViewModel.AdminViewModel

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
}

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val repo = remember { UserRepositoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }
    val adminViewModel = remember { AdminViewModel() }

    val firebaseUser = userViewModel.getCurrentUser()
    val userData by userViewModel.users.observeAsState()
    
    // Load cars from Firebase instead of hardcoded list
    val carsFromFirebase by adminViewModel.cars.observeAsState(emptyList())

    // Fetch user data on first composition
    LaunchedEffect(Unit) {
        firebaseUser?.uid?.let { uid ->
            userViewModel.getUserFromDatabase(uid) { success, message, _ ->
                if (!success) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Load cars from Firebase
        adminViewModel.loadCars()
    }

    val profileImage = painterResource(id = R.drawable.logo)

    data class Brand(val name: String, val imageRes: Int)

    val carBrands = listOf(
        Brand("Toyota", R.drawable.toyota),
        Brand("Nissan", R.drawable.nissan),
        Brand("Porsche", R.drawable.porsche),
        Brand("Audi", R.drawable.audi),
    )

    // Convert CarModel to Car for compatibility with existing BookingActivity
    val cars = carsFromFirebase.map { carModel ->
        Car(
            name = carModel.name,
            brand = carModel.brand,
            imageRes = 0, // We'll use imageUrl instead
            pricePerDay = carModel.pricePerDay,
            rating = carModel.rating,
            description = carModel.description
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Row (Profile and Settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = R.drawable.logo,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
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

            IconButton(onClick = {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Popular Brands", color = Color.White, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(carBrands) { brand ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = brand.imageRes,
                        contentDescription = brand.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .clickable {
                                val intent = when (brand.name) {
                                    "Toyota" -> Intent(context, ToyotaActivity::class.java)
                                    "BMW" -> Intent(context, BMWActivity::class.java)
                                    else -> null
                                }
                                intent?.let { context.startActivity(it) }
                            },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = brand.name,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Available Cars", color = Color.White, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(carsFromFirebase) { carModel ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color(0xFF1C1C1E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        // Use AsyncImage for Cloudinary URLs
                        AsyncImage(
                            model = carModel.imageUrl,
                            contentDescription = "Car Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.logo), // Fallback image
                            placeholder = painterResource(id = R.drawable.logo)
                        )
                        Button(
                            onClick = {
                                val intent = Intent(context, BookingActivity::class.java).apply {
                                    putExtra("car_name", carModel.name)
                                    putExtra("car_brand", carModel.brand)
                                    putExtra("car_image_url", carModel.imageUrl) // Pass URL instead of resource ID
                                    putExtra("car_price_per_day", carModel.pricePerDay)
                                    putExtra("car_rating", carModel.rating)
                                    putExtra("car_description", carModel.description)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Text(text = "Book Now", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = carModel.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Brand: ${carModel.brand}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = carModel.pricePerDay,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Rating: ${carModel.rating} / 5.0",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    VroomTrackTheme {
        DashboardScreen()
    }
}