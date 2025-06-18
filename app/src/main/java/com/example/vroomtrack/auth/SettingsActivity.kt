package com.example.vroomtrack.auth

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.model.UserDetailModel
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.example.vroomtrack.ViewModel.UserDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel // Import for viewModel()
import com.google.firebase.auth.EmailAuthProvider // Import for reauthentication

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VroomTrackTheme {
                SettingsScreenExpandable()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenExpandable(viewModel: UserDetailViewModel = viewModel()) { // Inject ViewModel
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid

    // State for expandable cards
    var expandedPersonal by remember { mutableStateOf(false) }
    var expandedPassword by remember { mutableStateOf(false) }
    var expandedVersion by remember { mutableStateOf(false) }

    // Personal details states (now initialized from observed ViewModel data)
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var maritalStatusExpanded by remember { mutableStateOf(false) }
    var maritalStatus by remember { mutableStateOf("Select marital status") }
    val maritalStatusOptions = listOf("Single", "Married", "Divorced", "Widowed")
    var cardInfo by remember { mutableStateOf("") }

    // Password states
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // --- Load user details when the screen is first composed or userId changes ---
    LaunchedEffect(userId) {
        if (!userId.isNullOrEmpty()) {
            viewModel.getUserDetails(userId)
        }
    }

    // --- Observe user details from ViewModel and update local states ---
    val userDetails by viewModel.userDetails.collectAsState(initial = null)

    LaunchedEffect(userDetails) {
        userDetails?.let {
            name = it.name ?: ""
            address = it.address ?: ""
            phoneNumber = it.phone ?: ""
            maritalStatus = it.maritalStatus ?: "Select marital status"
            cardInfo = it.cardInfo ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Custom back button row at the very top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                .clickable { activity?.finish() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 20.sp,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            ExpandableCard(
                title = "Personal Details",
                expanded = expandedPersonal,
                onCardArrowClick = { expandedPersonal = !expandedPersonal }
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Marital status dropdown
                Box {
                    OutlinedTextField(
                        value = maritalStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Marital Status") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (maritalStatusExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowRight,
                                contentDescription = null,
                                modifier = Modifier.clickable { maritalStatusExpanded = !maritalStatusExpanded },
                                tint = Color.White
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { maritalStatusExpanded = !maritalStatusExpanded },
                        colors = fieldColors()
                    )
                    DropdownMenu(
                        expanded = maritalStatusExpanded,
                        onDismissRequest = { maritalStatusExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray)
                    ) {
                        maritalStatusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = {
                                    maritalStatus = option
                                    maritalStatusExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cardInfo,
                    onValueChange = { cardInfo = it },
                    label = { Text("Card Info") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                Button(
                    onClick = {
                        if (!userId.isNullOrEmpty()) {
                            val user = UserDetailModel(
                                userId = userId,
                                name = name,
                                address = address,
                                phone = phoneNumber,
                                maritalStatus = maritalStatus,
                                cardInfo = cardInfo
                            )
                            viewModel.saveDetails(user) { success ->
                                val msg = if (success) "Personal details updated successfully!" else "Failed to update personal details."
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Personal Info")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableCard(
                title = "Update Password",
                expanded = expandedPassword,
                onCardArrowClick = { expandedPassword = !expandedPassword }
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (currentUser != null && currentUser.email != null) {
                            if (currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                                currentUser.reauthenticate(credential)
                                    .addOnCompleteListener { reauthTask ->
                                        if (reauthTask.isSuccessful) {
                                            currentUser.updatePassword(newPassword)
                                                .addOnCompleteListener { updateTask ->
                                                    if (updateTask.isSuccessful) {
                                                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                                        currentPassword = ""
                                                        newPassword = ""
                                                    } else {
                                                        Toast.makeText(context, "Failed to update password: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                        } else {
                                            Toast.makeText(context, "Re-authentication failed. Please check your current password: ${reauthTask.exception?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Please enter both current and new passwords.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "User not logged in or email not available.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Password")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableCard(
                title = "Version Info",
                expanded = expandedVersion,
                onCardArrowClick = { expandedVersion = !expandedVersion }
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("App Version: 1.0.0", color = Color.LightGray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "This is a demo version of VroomTrack app.",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ExpandableCard(
    title: String,
    expanded: Boolean,
    onCardArrowClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCardArrowClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White
                )
            }

            if (expanded) {
                Divider(color = Color.Gray, thickness = 1.dp)
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color.Gray,
    unfocusedContainerColor = Color.DarkGray,
    focusedLabelColor = Color.LightGray,
    unfocusedLabelColor = Color.Gray,
    focusedBorderColor = Color.LightGray,
    unfocusedBorderColor = Color.Gray
)

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    VroomTrackTheme {
        SettingsScreenExpandable()
    }
}