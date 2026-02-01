package com.example.apitestdog

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.apitestdog.ui.theme.ApiTestDogTheme
import com.example.apitestdog.network.DogApiClient
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApiTestDogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DogBreedScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DogBreedScreen(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val apiService = DogApiClient.apiService
    val context = LocalContext.current

    var inputBreed by remember { mutableStateOf("") }
    var knownBreeds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getBreedsList()
            }
            knownBreeds = response.message.keys
        } catch (t: Throwable) {
            errorMessage = t.message ?: "Failed to load breeds"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = inputBreed,
            onValueChange = { inputBreed = it },
            label = { Text("Dog breed") },
            singleLine = true,
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val breed = inputBreed.trim().lowercase()
                if (breed.isBlank()) {
                    errorMessage = "Please enter a breed"
                    imageUrls = emptyList()
                    return@Button
                }

                if (knownBreeds.isNotEmpty() && breed !in knownBreeds) {
                    errorMessage = "Unknown breed: $breed"
                    imageUrls = emptyList()
                    return@Button
                }

                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        val urls = withContext(Dispatchers.IO) {
                            apiService.getRandomImagesByBreed(breed).message
                        }
                        imageUrls = urls
                    } catch (t: Throwable) {
                        errorMessage = t.message ?: "Failed to load images"
                        imageUrls = emptyList()
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
        ) {
            Text("Search")
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!)
        }

        if (isLoading) {
            Text(text = "Loading...")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(imageUrls) { url ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    factory = {
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    update = { imageView ->
                        Picasso.get()
                            .load(url)
                            .fit()
                            .centerCrop()
                            .into(imageView)
                    },
                )
            }
        }
    }
}
