package com.example.vroomtrack.auth


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.R
import com.example.vroomtrack.Car
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
    val context = LocalContext.current
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
            name = "McLaren Sienna",
            brand = "McLaren",
            imageRes = R.drawable.supra,
            pricePerDay = "$4000/day",
            rating = 4.2,
            description = "The McLaren Senna is a track-focused hypercar named after legendary F1 driver Ayrton Senna. Built for extreme performance, it features a 4.0L twin-turbo V8 engine, lightweight carbon fiber body, and advanced aerodynamics, making it one of the most aggressive and exhilarating cars McLaren has ever produced.."
        ),
        Car(
            name = "Porsche 911",
            brand = "Porsche",
            imageRes = R.drawable.supra,
            pricePerDay = "$130/day",
            rating = 4.2,
            description = "The Porsche 911 is an iconic sports car known for its timeless design, rear-engine layout, and exceptional driving dynamics."
        ),
        Car(
            name = "Koyete ikena",
            brand = "Koyete",
            imageRes = R.drawable.supra,
            pricePerDay = "$80/day",
            rating = 4.2,
            description = "A sleek electric sports car with 600 hp and dual-motor AWD, the Koyte Vortex X1 blends futuristic design with raw performance—going 0–100 km/h in just 3.1 seconds.."
        ),
        Car(
            name = "Toyota Supra",
            brand = "Toyota",
            imageRes = R.drawable.supra,
            pricePerDay = "$80/day",
            rating = 4.2,
            description = "The Toyota Supra is an iconic sports car, renowned for its inline-six engine, balanced chassis, and distinctive design, offering an exhilarating driving experience."
        ),
        Car(
            name = "Toyota GR Corolla",
            brand = "Toyota",
            imageRes = R.drawable.supra,
            pricePerDay = "$200/day",
            rating = 4.2,
            description = "The Toyota GR Corolla is a rally-inspired hot hatch from Toyota's Gazoo Racing (GR) division, packing serious performance into a practical five‑door hatchback."
        ),
        Car(
            name = "Toyota Supra",
            brand = "Toyota",
            imageRes = R.drawable.supra,
            pricePerDay = "$80/day",
            rating = 4.2,
            description = "The Toyota Supra is an iconic sports car, renowned for its inline-six engine, balanced chassis, and distinctive design, offering an exhilarating driving experience."
        ),
    )

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
    )
{ paddingValues ->
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
                            .clickable {
                                val intent = Intent(context, BookingActivity::class.java).apply {
                                    putExtra("car_name", car.name)
                                    putExtra("car_brand", car.brand)
                                    putExtra("car_image_res_id", car.imageRes)
                                    putExtra("car_price_per_day", car.pricePerDay)
                                    putExtra("car_rating", car.rating)
                                    putExtra("car_description", car.description)
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        Image(
                            painter = painterResource(id = car.imageRes),
                            contentDescription = "Car Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
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