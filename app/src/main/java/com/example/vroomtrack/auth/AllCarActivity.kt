package com.example.vroomtrack.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.R
import com.example.vroomtrack.Car // Ensure you have this Car data class
import com.example.vroomtrack.ui.theme.VroomTrackTheme

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

    val cars = listOf(
        Car(
            name = "Audi RS6",
            brand = "Audi",
            imageRes = R.drawable.rs6,
            pricePerDay = "$50/day",
            rating = 4.5,
            description = "The Audi RS 6 Avant is a high-performance variant of the A6, known for its powerful engine, quattro all-wheel drive, and spacious wagon body style."
        ),
        Car(
            name = "Nissan GTR",
            brand = "Nissan",
            imageRes = R.drawable.nissangtr,
            pricePerDay = "$70/day",
            rating = 4.0,
            description = "The Nissan GT-R, often dubbed 'Godzilla', is a legendary high-performance sports car celebrated for its raw power, advanced all-wheel-drive system, and track capabilities."
        ),
        Car(
            name = "BMW M5",
            brand = "R.drawable.bmwm5, M5", // Corrected brand name
            imageRes = R.drawable.bmwm5,
            pricePerDay = "$65/day",
            rating = 4.8,
            description = "The BMW M5 is a high-performance version of the BMW 5 Series sedan, known for its powerful V8 engine, luxurious interior, and exceptional driving dynamics."
        ),
        Car(
            name = "Toyota Supra",
            brand = "Toyota",
            imageRes = R.drawable.supra,
            pricePerDay = "$80/day",
            rating = 4.2,
            description = "The Toyota Supra is an iconic sports car, renowned for its inline-six engine, balanced chassis, and distinctive design, offering an exhilarating driving experience."
        ),
        // Add more cars if you have them, or dynamically fetch them
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Available Cars", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black // Set background color for the entire screen
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cars) { car ->
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
                        Image(
                            painter = painterResource(id = car.imageRes),
                            contentDescription = "Car Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // You can add a Book Now button here as well, similar to DashboardScreen
                    }

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
                            text = car.pricePerDay,
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
    }
}

@Preview(showBackground = true)
@Composable
fun AllCarsScreenPreview() {
    VroomTrackTheme {
        AllCarsScreen()
    }
}