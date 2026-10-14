package com.example.newsrecommendation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.newsrecommendation.api.RecommendationRequest
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.RecommendationItem
import com.example.newsrecommendation.ui.theme.NewsRecommendationTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen(){
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Đăng Nhập")

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = {email = it},
            label = { Text("Email")},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isEmpty()) {
                    errorMessage = "Bạn chưa nhập email"
                } else if (password.isEmpty()) {
                    errorMessage = "Bạn chưa nhập mật khẩu"
                } else {
                    errorMessage = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Đăng nhập")
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

//@Composable
//fun RecommendationScreen() {
//    var articles by remember {
//        mutableStateOf<List<RecommendationItem>>(emptyList())
//    }
//    var message by remember { mutableStateOf("Đang tải tin tức...") }
//    var loading by remember { mutableStateOf(true) }
//
//    LaunchedEffect(Unit) {
//        try {
//            val response = RetrofitClient.apiService.getRecommendations(
//                RecommendationRequest(
//                    user_id = 1,
//                    start_date = "2026-09-01",
//                    end_date = "2026-10-07",
//                    top_k = 10
//                )
//            )
//
//            if (response.isSuccessful) {
//                articles = response.body()?.results.orEmpty()
//                message = if (articles.isEmpty()) {
//                    "Không tìm thấy bài báo."
//                } else {
//                    ""
//                }
//            } else {
//                message = "API trả về lỗi: ${response.code()}"
//            }
//        } catch (e: Exception) {
//            message = "Lỗi kết nối: ${e.message}"
//        } finally {
//            loading = false
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        Text(
//            text = "Tin tức dành cho bạn",
//            style = MaterialTheme.typography.headlineSmall
//        )
//
//        Text(
//            text = "Khoảng thời gian: 01/09/2026 – 07/10/2026"
//        )
//
//        if (loading) {
//            CircularProgressIndicator()
//        } else if (message.isNotBlank()) {
//            Text(text = message)
//        }
//
//        LazyColumn(
//            modifier = Modifier.weight(1f),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            items(articles) { article ->
//                Card(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Text(
//                            text = article.title,
//                            style = MaterialTheme.typography.titleMedium
//                        )
//
//                        if (article.description.isNotBlank()) {
//                            Text(
//                                text = article.description,
//                                maxLines = 3,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
//
//                        Text(
//                            text = "Chủ đề: ${article.category ?: "Chưa phân loại"}"
//                        )
//
//                        Text(
//                            text = "Điểm cá nhân hóa: ${
//                                String.format(
//                                    Locale.US,
//                                    "%.4f",
//                                    article.personalized_score
//                                )
//                            }"
//                        )
//                    }
//                }
//            }
//        }
//    }
//}