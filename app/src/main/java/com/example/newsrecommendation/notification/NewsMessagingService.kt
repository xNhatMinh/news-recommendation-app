package com.example.newsrecommendation.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat

class NewsMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(
            "FCM_MESSAGE",
            message.notification?.body.orEmpty()
        )
        showNewsNotification(message)
    }

    private fun showNewsNotification(message: RemoteMessage) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(this, "news_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(message.notification?.title ?: "Tin mới")
            .setContentText(message.notification?.body.orEmpty())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }
}