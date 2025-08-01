package com.example.front

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.database.FirebaseDatabase

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var mediaPlayer: MediaPlayer? = null
    private val CHANNEL_ID = "emergency_alert_channel"
    private val NOTIFICATION_ID = 1

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "새 토큰 생성됨: $token")

        val preferenceManager = PreferenceManager(this)
        val username = preferenceManager.getUsername()
        val role = preferenceManager.getRole()
        val linkedUser = preferenceManager.getLinkedUser()

        if (username != null && role != null) {
            // 역할에 따라 다른 경로에 토큰 저장
            val tokenPath = when (role) {
                "caregiver" -> "caregiver_tokens"
                "senior" -> "senior_tokens"
                else -> "other_tokens"
            }
            
            // 토큰 저장
            FirebaseDatabase.getInstance().getReference(tokenPath)
                .child(username)
                .setValue(token)
                .addOnSuccessListener {
                    Log.d("FCM", "토큰 저장 성공: $username ($role)")
                    
                    // 보호자인 경우 연결된 피보호자 정보도 저장
                    if (role == "caregiver" && linkedUser != null) {
                        FirebaseDatabase.getInstance().getReference("caregiver_links")
                            .child(username)
                            .setValue(linkedUser)
                            .addOnSuccessListener {
                                Log.d("FCM", "보호자-피보호자 연결 정보 저장 성공")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "토큰 저장 실패", e)
                }
        } else {
            Log.e("FCM", "사용자 정보가 없어 토큰을 저장할 수 없습니다.")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "메시지 수신: ${remoteMessage.data}")

        // 앱이 포그라운드에 있을 때만 직접 알림을 표시
        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title ?: "긴급 호출"
            val body = remoteMessage.notification?.body ?: "보호자로부터 긴급 호출이 왔습니다!"
            showNotification(title, body)
            playSirenSound()  // 앱이 포그라운드일 때 siren.mp3 재생
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // siren.mp3의 URI 생성
        val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.siren)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "긴급 호출 채널",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
                description = "보호자 호출 시 울리는 긴급 알림 채널"
                setSound(soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)  // 알림이 자동으로 사라지지 않도록 설정
            .setOngoing(true)      // 진행 중인 알림으로 설정
            .setSound(soundUri)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun playSirenSound() {
        try {
            // 이전 MediaPlayer가 있다면 정리
            mediaPlayer?.release()

            // 새로운 MediaPlayer 생성
            mediaPlayer = MediaPlayer.create(this, R.raw.siren)
            mediaPlayer?.apply {
                isLooping = true  // 무한 반복 재생
                setVolume(1.0f, 1.0f)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                start()
            }
        } catch (e: Exception) {
            Log.e("FCM", "알람 소리 재생 실패", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}