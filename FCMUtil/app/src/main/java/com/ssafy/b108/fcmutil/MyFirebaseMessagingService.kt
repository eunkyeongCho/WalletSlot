package com.ssafy.b108.fcmutil

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "새로운 FCM 토큰: $token")
        // 👉 여기서 토큰을 서버로 전송하거나 저장 가능
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "푸시 메시지 수신: ${remoteMessage.data}")

        val channelId = "default_channel"
        val channelName = "기본 알림"

        // Android 8.0 이상에서는 채널을 반드시 생성해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // 알림 내용 구성
        val title = remoteMessage.notification?.title ?: "알림"
        val body = remoteMessage.notification?.body ?: "내용 없음"

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ✅ 아이콘은 반드시 있어야 함
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)) {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    this@MyFirebaseMessagingService,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(System.currentTimeMillis().toInt(), notification)
            } else {
                Log.w("FCM", "알림 권한이 없어 notify() 실행 안 함")
            }
        }

        // 알림 띄우기
        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}