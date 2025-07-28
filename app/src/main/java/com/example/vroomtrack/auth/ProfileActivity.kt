package com.example.vroomtrack.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.vroomtrack.Repository.UserRepositoryImpl
import com.example.vroomtrack.ViewModel.UserViewModel
import com.example.vroomtrack.ui.theme.VroomTrackTheme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VroomTrackTheme {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun ProfileScreen() {

    val context = LocalContext.current
    val repo = remember { UserRepositoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }

    val firebaseUser = userViewModel.getCurrentUser()
    val userData by userViewModel.users.observeAsState()

    LaunchedEffect(Unit) {
        firebaseUser?.uid?.let { uid ->
            userViewModel.getUserFromDatabase(uid) { userModel, errorMsg ->
                if (userModel == null) {
                    Toast.makeText(context, errorMsg ?: "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }





}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    VroomTrackTheme {
        ProfileScreen()
    }
}