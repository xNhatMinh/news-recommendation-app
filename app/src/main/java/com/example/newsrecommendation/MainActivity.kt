package com.example.newsrecommendation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.ui.theme.NewsRecommendationTheme
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            NewsRecommendationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    HealthCheckScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HealthCheckScreen(
    modifier: Modifier = Modifier
) {
    var message by remember {
        mutableStateOf("Đang kết nối...")
    }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.healthCheck()

            if (response.isSuccessful) {
                message = "Backend: ${response.body()?.status}"
            } else {
                message = "Backend lỗi: ${response.code()}"
            }

        } catch (e: Exception) {
            message = "Không thể kết nối: ${e.message}"
        }
    }

    Text(
        text = message,
        modifier = modifier
    )
}