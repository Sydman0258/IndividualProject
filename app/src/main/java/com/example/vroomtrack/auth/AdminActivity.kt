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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert // For the dropdown icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.vroomtrack.ViewModel.CarViewModel
import com.example.vroomtrack.model.CarModel
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.text.style.TextAlign
import com.example.vroomtrack.LoginActivity

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VroomTrackTheme {
                AdminScreen(onLogout = {
                    // Handle logout logic here
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java) // Assuming LoginActivity is your login screen
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val carViewModel: CarViewModel = viewModel()
    val cars by carViewModel.cars.collectAsState()

    var showMenu by remember { mutableStateOf(false) } // State for dropdown menu visibility

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E88E5), titleContentColor = Color.White),
                actions = {
                    // Dropdown menu icon
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More actions",
                            tint = Color.White
                        )
                    }

                    // DropdownMenu
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                                  // Separator
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                onLogout()
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF0F0F0))
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Welcome, Admin!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Use the admin panel to manage cars, bookings, and user statistics.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button to Add Cars
            Button(
                onClick = {
                    context.startActivity(Intent(context, AddCarActivity::class.java))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
            ) {
                Text("Add New Car", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // List of Cars
            if (cars.isEmpty()) {
                Text("No cars available. Add some!", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                Text(
                    text = "Current Cars:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cars) { car ->
                        CarListItem(car = car,
                            onEditClick = {
                                val intent = Intent(context, AddCarActivity::class.java).apply {
                                    putExtra("carId", it.id) // Pass car ID for editing
                                }
                                context.startActivity(intent)
                            },
                            onDeleteClick = {
                                carViewModel.deleteCar(it.id) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CarListItem(car: CarModel, onEditClick: (CarModel) -> Unit, onDeleteClick: (CarModel) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Optional: view car details */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Car Image
            AsyncImage(
                model = car.imageUrl,
                contentDescription = "${car.brand} ${car.name}",
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray), // Placeholder background
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${car.brand} ${car.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = "Price: $${car.pricePerDay} / day",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Rating: ${car.rating}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                // Assuming car.available is a Boolean property in CarModel
                Text(
                    text = if (car.available) "Available" else "Not Available",
                    fontSize = 12.sp,
                    color = if (car.available) Color.Green else Color.Red
                )
            }
            Row {
                IconButton(onClick = { onEditClick(car) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Car", tint = Color(0xFF1E88E5))
                }
                IconButton(onClick = { onDeleteClick(car) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Car", tint = Color(0xFFF44336))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AdminScreenPreview() {
    VroomTrackTheme {
        AdminScreen(onLogout = {}) // Provide an empty lambda for preview
    }
}