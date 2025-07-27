package com.example.vroomtrack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.auth.AdminActivity
import com.example.vroomtrack.auth.DashboardActivity
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VroomTrackTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val Black = Color(0xFF000000)
    val context = LocalContext.current

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf(TextFieldValue("")) }
    var showAnimation by remember { mutableStateOf(false) }

    if (showAnimation) {
        CarAnimationScreen {
            context.startActivity(Intent(context, DashboardActivity::class.java))
            showAnimation = false
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("VR.O.OM Track", fontSize = 24.sp, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    val icon = if (passwordVisible)
                        painterResource(id = R.drawable.baseline_visibility_24)
                    else
                        painterResource(id = R.drawable.baseline_visibility_off_24)
                    Icon(
                        painter = icon,
                        contentDescription = "Toggle Password Visibility",
                        tint = Color.White,
                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { rememberMe = !rememberMe }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = Color.Black,
                        checkedColor = Color.White,
                        uncheckedColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Remember Me", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseDatabase.getInstance().reference

                    if (email.text.isBlank() || password.text.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    auth.signInWithEmailAndPassword(email.text, password.text)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                                db.child("users").child(uid).child("admin").get()
                                    .addOnSuccessListener { snapshot ->
                                        val isAdmin = snapshot.getValue(Boolean::class.java) == true
                                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()

                                        if (isAdmin) {
                                            context.startActivity(Intent(context, AdminActivity::class.java))
                                        } else {
                                            showAnimation = true
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to retrieve user role", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Login", color = Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Forgot Password?",
                color = Color.White,
                modifier = Modifier.clickable {
                    showResetDialog = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val annotatedText = buildAnnotatedString {
                append("Not a user? ")
                pushStringAnnotation(tag = "REGISTER", annotation = "register")
                withStyle(style = SpanStyle(color = Color.White, textDecoration = TextDecoration.Underline)) {
                    append("Register")
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "REGISTER", start = offset, end = offset)
                        .firstOrNull()?.let {
                            context.startActivity(Intent(context, RegistrationActivity::class.java))
                        }
                },
                style = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp)
            )
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Password") },
                text = {
                    Column {
                        Text("Enter your email address to receive a reset link.", color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFF8217C5),
                                unfocusedIndicatorColor = Color.Gray,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (resetEmail.text.isBlank()) {
                            Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        FirebaseAuth.getInstance().sendPasswordResetEmail(resetEmail.text)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Reset email sent", Toast.LENGTH_SHORT).show()
                                    showResetDialog = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CarAnimationScreen(onAnimationComplete: () -> Unit) {
    val context = LocalContext.current
    val screenWidth = context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density

    val carWidth = 450f
    val animationDuration = 3000

    var animationStarted by remember { mutableStateOf(false) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = if (animationStarted) screenWidth + carWidth else -carWidth,
        animationSpec = tween(durationMillis = animationDuration)
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(animationDuration.toLong())
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painterResource(id = R.drawable.car),
            contentDescription = "Car Animation",
            modifier = Modifier
                .offset(x = animatedOffsetX.dp)
                .height(300.dp)
                .width(450.dp),
            contentScale = ContentScale.Fit
        )
    }
}
