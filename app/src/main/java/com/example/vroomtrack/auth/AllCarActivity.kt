package com.example.vroomtrack.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.vroomtrack.model.CarModel
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.google.firebase.database.*

class AllCarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VroomTrackTheme {
                AllCarsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCarsScreen() {
    val context = LocalContext.current

    var cars by remember { mutableStateOf(listOf<CarModel>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Firebase DB reference to "cars" node
    LaunchedEffect(Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("cars")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val carList = mutableListOf<CarModel>()
                for (carSnap in snapshot.children) {
                    val car = carSnap.getValue(CarModel::class.java)
                    if (car != null && car.available) {
                        carList.add(car.copy(id = carSnap.key ?: ""))
                    }
                }

                cars = carList
                isLoading = false
                errorMessage = null
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage = error.message
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = {
            val activity = LocalActivity.current
            TopAppBar(
                title = { Text("All Available Cars", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = "Error loading cars: $errorMessage",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp
                    )
                }
                cars.isEmpty() -> {
                    Text(
                        text = "No cars available.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cars) { car ->
                            CarListItem(car = car) {
                                // Navigate to BookingActivity with car details
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
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CarListItem(car: CarModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            if (car.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = car.imageUrl,
                    contentDescription = "${car.brand} ${car.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = car.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = car.brand,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = car.pricePerDay,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = "Rating: ${car.rating} / 5.0",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = if (car.available) "Available" else "Not Available",
                    fontSize = 12.sp,
                    color = if (car.available) Color.Green else Color.Red
                )
            }
        }
    }
}
