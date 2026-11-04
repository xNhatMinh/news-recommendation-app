package com.example.newsrecommendation.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationManager
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.content.Intent
import com.example.newsrecommendation.MainActivity
import com.example.newsrecommendation.R
import androidx.core.content.ContextCompat

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

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("screen", message.data["screen"] ?: "notifications")

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = message.notification?.title ?: message.data["title"] ?: "Tin mới"
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        val tag = message.data["notification_tag"]
            ?: message.notification?.tag
            ?: message.data["article_url"]
            ?: message.messageId
            ?: "newsora_${System.currentTimeMillis()}"

        val notification = NotificationCompat.Builder(this, "news_alerts")
            .setSmallIcon(R.drawable.ic_stat_newsora)
            .setColor(ContextCompat.getColor(this, R.color.newsora_notification))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSubText(message.data["topic"])
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(tag, 0, notification)
    }
}
