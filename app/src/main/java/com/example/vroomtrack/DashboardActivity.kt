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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vroomtrack.R
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Car(val imageResId: Int, val name: String)

val carList = listOf(
    Car(R.drawable.toyota, "Toyota"),
    Car(R.drawable.nissan, "Nissan"),
    // You can add more cars here as needed:
    // Car(R.drawable.some_other_car_image, "Other Car Name"),
)

class DashboardActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Auth and Firestore
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // State to hold the fetched username
        var usernameFromFirestore by mutableStateOf<String?>(null)
        var isLoadingUsername by mutableStateOf(true)

        // Fetch username from Firestore
        auth.currentUser?.uid?.let { userId ->
            val userProfileRef = db.collection("artifacts")
                .document(application.packageName)
                .collection("users")
                .document(userId)
                .collection("user_profiles")
                .document("profile")

            userProfileRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        usernameFromFirestore = documentSnapshot.getString("username")
                    } else {
                        // Document doesn't exist, maybe user registered without username or data is missing
                        usernameFromFirestore = "User" // Default if not found
                    }
                    isLoadingUsername = false
                }
                .addOnFailureListener { e ->
                    println("Error fetching user profile: ${e.localizedMessage}")
                    usernameFromFirestore = "Error Loading" // Indicate an error occurred
                    isLoadingUsername = false
                }
        } ?: run {
            // No user logged in
            usernameFromFirestore = "Guest"
            isLoadingUsername = false
        }

        setContent {
            VroomTrackTheme {
                DashboardScreen(
                    displayName = usernameFromFirestore, // Pass the fetched username
                    isLoadingUsername = isLoadingUsername // Pass loading state for username
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(displayName: String?, isLoadingUsername: Boolean) { // Accept displayName and loading state
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
                            // Display the dynamic username here
                            if (isLoadingUsername) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = displayName ?: "Unknown User", // Use the displayName, with fallback
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = Color.White
                                )
                            }
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
        // Provide a dummy displayName and set loading to false for the preview
        DashboardScreen(displayName = "Preview User", isLoadingUsername = false)
    }
}
