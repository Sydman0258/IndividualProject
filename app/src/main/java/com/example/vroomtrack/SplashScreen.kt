package com.example.vroomtrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.vroomtrack.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VroomTrackTheme {
                val context = LocalContext.current

                val navigateToRegistration = {
                    startActivity(Intent(context, RegistrationActivity::class.java))
                    finish()
                }

                val navigateToLogin = {
                    startActivity(Intent(context, LoginActivity::class.java))
                    finish()
                }

                SplashContent(
                    onRegisterClick = navigateToRegistration,
                    onLoginClick = navigateToLogin
                )
            }
        }
    }
}

@Composable
fun SplashContent(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    var showLoader by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    val MyFontFamily = FontFamily(
        Font(R.font.lequire, FontWeight.Bold)
    )

    val coroutineScope = rememberCoroutineScope()

    val buttonAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500)
    )

    val textYOffset by animateDpAsState(
        targetValue = if (startAnimation) (-150).dp else 0.dp,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
    )

    LaunchedEffect(Unit) {
        delay(800)
        startAnimation = true
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.supra),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .offset(y = textYOffset)
        ) {
            Text(
                text = "VR.O.OM",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "TRACK",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = MyFontFamily,

                    color = Color.White,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.offset(y = (-8).dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.8f)
                .padding(bottom = 32.dp)
                .alpha(buttonAlpha)
        ) {
            Button(
                onClick = {
                    isNavigating = true
                    showLoader = false

                    coroutineScope.launch {
                        val threshold = 2000L
                        val simulatedDelay = 3500L
                        val startTime = System.currentTimeMillis()

                        val job = launch {
                            delay(simulatedDelay) // simulate slow network
                        }

                        delay(threshold)
                        if (job.isActive) {
                            showLoader = true
                        }

                        job.join()
                        showLoader = false
                        onRegisterClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Register", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isNavigating = true
                    showLoader = false

                    coroutineScope.launch {
                        val threshold = 2000L
                        val simulatedDelay = 1500L
                        val startTime = System.currentTimeMillis()

                        val job = launch {
                            delay(simulatedDelay) // simulate faster network
                        }

                        delay(threshold)
                        if (job.isActive) {
                            showLoader = true
                        }

                        job.join()
                        showLoader = false
                        onLoginClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
            ) {
                Text("Login", color = Color.White)
            }
        }

        if (showLoader && isNavigating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
