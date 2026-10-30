package com.example.newsrecommendation

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.newsrecommendation.ui.navigation.AppNavHost
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.example.newsrecommendation.ui.theme.NewsRecommendationTheme
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import android.app.NotificationChannel
import android.app.NotificationManager

class MainActivity : ComponentActivity() {
    private val openNotifications = mutableStateOf(false)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private fun createNewsChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "news_alerts",
                "Tin tức",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openNotifications.value = intent?.getStringExtra("screen") == "notifications"
        createNewsChannel()
        if (Build.VERSION.SDK_INT >= 33) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (
                ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(permission)
            }
        }
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {
                Log.d("FCM_TOKEN", it)
            }
            .addOnFailureListener {
                Log.e("FCM_TOKEN", "Không lấy được token", it)
            }
        setContent {
            NewsRecommendationTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    AppNavHost(
                        openNotifications = openNotifications.value,
                        onNotificationHandled = {
                            openNotifications.value = false
                            intent?.removeExtra("screen")
                        }
                    )
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openNotifications.value = intent.getStringExtra("screen") == "notifications"
    }

}

