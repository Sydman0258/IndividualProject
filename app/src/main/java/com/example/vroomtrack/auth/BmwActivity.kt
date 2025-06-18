package com.example.vroomtrack.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vroomtrack.R // Ensure you have R.drawable.bmwm3e46 for this to work
import com.example.vroomtrack.ui.theme.VroomTrackTheme

class BMWActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VroomTrackTheme {
                BMWScreen(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun BMWScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BMW Cars",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.bmwactivity),
            contentDescription = "BMW Car",
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "BMW, or Bavarian Motor Works, is renowned for its luxury vehicles and motorcycles. " +
                    "Known for their driving dynamics, performance, and engineering, BMW cars like the " +
                    "M3 E46 have achieved iconic status among enthusiasts. BMW blends sporty elegance " +
                    "with innovative technology, embodying the brand's tagline: 'The Ultimate Driving Machine'.",
            color = Color.Gray,
            textAlign = TextAlign.Justify
        )
    }
}

@Preview
@Composable
fun BMWScreenPreview() {
    VroomTrackTheme {
        BMWScreen(onBackClick = {})
    }}