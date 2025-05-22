package com.example.vroomtrack

import android.content.Intent // Import Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                SplashContent(
                    onAnimationComplete = {
                        // Navigate to LoginActivity
                        val intent = Intent(this@SplashScreen, LoginActivity::class.java)
                        startActivity(intent)
                        finish() // Finish the splash screen activity so user can't go back to it
                    }
                )
            }
        }
    }
}

@Composable
fun SplashContent(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF121212),  // Dark grey
            Color(0xFF424242),  // Medium grey
            Color(0xFF9E9E9E)   // Light grey
        )
    )

    LaunchedEffect(Unit) {
        scale.animateTo(1.05f, tween(500))
        delay(3000)
        scale.animateTo(1f, tween(500))
        onAnimationComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
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
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    VroomTrackTheme {
        SplashContent(onAnimationComplete = {})
    }
}