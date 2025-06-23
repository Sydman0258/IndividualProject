package com.example.vroomtrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState // Import for animating Dp values
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing // Good easing for slide animation
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset // Import for offset modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

    val buttonAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500)
    )

    // Animate the Y offset for the text
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
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "TRACK",
                style = MaterialTheme.typography.displayMedium.copy(
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
                onClick = onRegisterClick,
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
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
            ) {
                Text("Login", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    VroomTrackTheme {
        SplashContent(
            onRegisterClick = {},
            onLoginClick = {},
        )
    }
}