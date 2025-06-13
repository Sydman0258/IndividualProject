package com.example.vroomtrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.ui.theme.VroomTrackTheme

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
                    onLoginClick = navigateToLogin // Corrected parameter name
                )
            }
        }
    }
}

@Composable
fun SplashContent(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit, // Corrected parameter name
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Background Image
        Image(
            painter = painterResource(id = R.drawable.supra), // Assuming supra.jpg or supra.png
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,

        )


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.8f)

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



            Column(modifier= Modifier.padding(top= 350.dp)) {
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
                onClick = onLoginClick, // Corrected parameter name
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
            ) {
                Text("Login", color = Color.White)
            }}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    VroomTrackTheme {
        SplashContent(
            onRegisterClick = {},
            onLoginClick = {} // Corrected parameter name
        )
    }
}