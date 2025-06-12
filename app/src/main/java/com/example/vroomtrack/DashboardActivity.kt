package com.example.vroomtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // Import height for Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items // Import items for LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
// REMOVED: import androidx.compose.material3.OutlinedTextField
// REMOVED: import androidx.compose.material3.OutlinedTextFieldDefaults

import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.Surface // Import Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme // Import MaterialTheme for typography
import androidx.compose.runtime.Composable
// REMOVED: import androidx.compose.runtime.getValue
// REMOVED: import androidx.compose.runtime.mutableStateOf
// REMOVED: import androidx.compose.runtime.remember
// REMOVED: import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment // Import Alignment for verticalAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // Ensure Color is imported
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vroomtrack.R
import com.example.vroomtrack.ui.theme.VroomTrackTheme


data class Car(val imageResId: Int, val name: String)

val carList = listOf(
    Car(R.drawable.toyota, "Toyota"),
    Car(R.drawable.nissan, "Nissan"),
    // You can add more cars here as needed:
    // Car(R.drawable.some_other_car_image, "Other Car Name"),
)

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
    // REMOVED: var username by remember { mutableStateOf("") } // Removed state variable

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Scaffold(
            containerColor = Color.Black
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // This is your main header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left section: Image and Texts
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.supra),
                            contentDescription = "Car Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = "John Doe",
                                modifier = Modifier.padding(start = 8.dp),
                                color = Color.White
                            )
                            Text(
                                text = "Driver",
                                modifier = Modifier.padding(start = 8.dp),
                                color = Color.White
                            )
                        }
                    }

                    // Right section: Settings Icon
                    IconButton(onClick = { /* Handle click action */ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }

                // Spacer after header, before "POPULAR CARS"
                // Adjusted spacing after removing the TextField
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "POPULAR CARS",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(carList) { car ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = car.imageResId),
                                contentDescription = car.name,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = car.name,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Optional: Remaining space below LazyRow
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Your dashboard content continues here...",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyBasicScaffoldScreenPreview() {
    VroomTrackTheme {
        DashboardScreen()
    }
}