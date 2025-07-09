package com.example.vroomtrack.auth

// Android imports
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vroomtrack.ViewModel.CarViewModel
import com.example.vroomtrack.ui.theme.VroomTrackTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AddCarActivity : ComponentActivity() {

    // Cloudinary constants - replace with your values
    private val cloudName = "dp0ca1yzs" // e.g. "mycloud123"
    private val uploadPreset = "sydman" // upload preset name you created in Cloudinary

    // OkHttp client for upload requests
    private val client = OkHttpClient()

    // Store the uploaded image URL here
    private var uploadedImageUrl by mutableStateOf("")

    // Launcher to pick image from gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // When image is picked, upload it to Cloudinary
            uploadImageToCloudinary(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VroomTrackTheme {
                AddEditCarScreen(
                    uploadedImageUrl = uploadedImageUrl,
                    onPickImage = { pickImageLauncher.launch("image/*") },
                    onUploadImage = { uri -> uploadImageToCloudinary(uri) },
                    onCarAdded = { finish() }
                )
            }
        }
    }


    private fun uploadImageToCloudinary(uri: Uri) {
        val context = this

        // Show some UI feedback could be added (like progress indicator)

        // Run upload in background
        val stream = contentResolver.openInputStream(uri) ?: run {
            Toast.makeText(context, "Unable to open image", Toast.LENGTH_SHORT).show()
            return
        }

        // Build multipart body
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "upload.jpg", stream.readBytes().toRequestBody(null, 0))
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        // Prepare request
        val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Enqueue async upload
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val json = JSONObject(response.body!!.string())
                        val imageUrl = json.getString("secure_url")
                        runOnUiThread {
                            uploadedImageUrl = imageUrl
                            Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarScreen(
    uploadedImageUrl: String,
    onPickImage: () -> Unit,
    onUploadImage: (android.net.Uri) -> Unit,
    onCarAdded: () -> Unit
) {
    val context = LocalContext.current
    val carViewModel: CarViewModel = viewModel()

    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf(uploadedImageUrl) } // Use Cloudinary URL
    var pricePerDay by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(uploadedImageUrl) {
        imageUrl = uploadedImageUrl // Update imageUrl state when upload completes
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Car") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E88E5), titleContentColor = Color.White)
            )
        }
    ) { padding ->

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color(0xFFF0F0F0))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        )
        {

            Text("Enter Car Details", style = MaterialTheme.typography.headlineSmall, color = Color.Black)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Car Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            // Show uploaded image URL or button to pick image
            if (imageUrl.isNotBlank()) {
                Text(text = "Image Uploaded", color = Color.Green)
                Text(text = imageUrl, color = Color.Blue, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                Button(onClick = onPickImage, modifier = Modifier.fillMaxWidth()) {
                    Text("Pick Image from Gallery")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pricePerDay,
                onValueChange = { newValue -> if (newValue.matches(Regex("^\\d*\\.?\\d*\$")) || newValue.isEmpty()) pricePerDay = newValue },
                label = { Text("Price Per Day") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = rating,
                onValueChange = { newValue -> if (newValue.matches(Regex("^\\d*\\.?\\d*\$")) || newValue.isEmpty()) rating = newValue },
                label = { Text("Rating (0.0-5.0)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val parsedRating = rating.toDoubleOrNull()
                    if (name.isBlank() || brand.isBlank() || imageUrl.isBlank() || pricePerDay.isBlank() || parsedRating == null || description.isBlank()) {
                        Toast.makeText(context, "Please fill all required fields correctly.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (parsedRating < 0.0 || parsedRating > 5.0) {
                        Toast.makeText(context, "Rating must be between 0.0 and 5.0", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    carViewModel.addCar(
                        name, brand, imageUrl, pricePerDay, parsedRating, description
                    ) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) onCarAdded()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5), contentColor = Color.White)
            ) {
                Text("Add Car", fontSize = 18.sp)
            }
        }
    }
}
