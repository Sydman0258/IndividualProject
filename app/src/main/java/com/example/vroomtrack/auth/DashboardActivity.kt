import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vroomtrack.Car
import com.example.vroomtrack.Repository.UserRepositoryImpl
import com.example.vroomtrack.ViewModel.CarViewModel
import com.example.vroomtrack.ViewModel.UserViewModel
import com.example.vroomtrack.auth.AllCarActivity
import com.example.vroomtrack.auth.BookingActivity
import com.example.vroomtrack.auth.SettingsActivity
import com.example.vroomtrack.auth.UserProfileActivity
import com.example.vroomtrack.ui.theme.VroomTrackTheme

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

    LaunchedEffect(Unit) {
        firebaseUser?.uid?.let { uid ->
            userViewModel.getUserFromDatabase(uid) { success, message, _ ->
                if (!success) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val cars by carViewModel.cars.collectAsState()

    val filteredCars = cars.filter { car ->
        car.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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
                    val intent = android.content.Intent(context, UserProfileActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                // Use your profile image here
                // Example placeholder circle:
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
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
                context.startActivity(android.content.Intent(context, SettingsActivity::class.java))
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1C1E), shape = MaterialTheme.shapes.small),
            placeholder = { Text("Search cars...", color = Color.Gray) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Color.White),
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        // Load car.imageUrl dynamically with Coil or similar
                        // Example placeholder box for image:
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.DarkGray)
                                .clickable {
                                    val intent = android.content.Intent(context, BookingActivity::class.java).apply {
                                        putExtra("car_id", car.id)
                                    }
                                    context.startActivity(intent)
                                }
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

        Text(
            text = "See More",
            color = Color(0xFF1E88E5),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable {
                    val intent = android.content.Intent(context, AllCarActivity::class.java)
                    context.startActivity(intent)
                }
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}
