package com.example.front

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class FallDetectionService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val accessToken = "ya29.c.c0ASRK0GYYfw1Y5IdVwnVqiNiVO6FZJy6wvG4o_2IzR6pvRUQ1LnU5oxtCoL_U-NoZjPMUJuzdbVWcXyPFn7qHf8o7rasj8mOi7p0WQgxRxqaCc3FGpj_QrDPfgAMx8odyVU7JjeegG_mPp-RM0A8MzvVrseh9CES3pRoUTQKU-OOC4uODGHE5bn0F9OqHS1IVQuyjRxVWv-UNjxWuY2gHILCJNT0LT-ImxBHpVP7T5lWg7izk-6rtvAZjKqhERqOdRdFE8CtuGuS_USsG8qPWxoEbaPk7sLBgzWbZw44ON9W5pHMCbtAL4A1ahNhz6jHiREcu-aPAEKWS0rF0jop7vuMd7la5wqr-yUUCByjOFLevOBkX6i_3n3rcL385K7rulMs03qymjWwBud0t32UVc_pZ-c_td-XR2tXBgdbsBxJYQMM2_fVp-8J57QJUvSulJYqwJnq0yJ1tBI6l0qBwJ380IvSOqij47Bh-p4pOx8Srk_dW6Zw-58txWhiW3696wJ__zUS8JO_9i9QIXnZlm6g9Utio-htZYOf-87YFvQ5Vtivh9VB0S_baXgVWwoxr-2VrpkthfWmJumMalQ5Y60QzfMqMibxFeWfcgdvtaiFOB6_nnSzQMeqIXyxjqh2FqgFu113-BR8wMieB0thRU-zna2Iw1UryMl6V5vmugcqYZ6FOxM1JikfqdM0lzWUfprjycJF3xRq4RgnlgpmjzpY7qJx54jzdtnIa0vt8ajyQRFgZFgOst8vbxy3e0a3Rgmmbrwd_JWiuetmi9SQQbcyp3ezQR27ubFnQasFBbcX1-mIFXwyh_b8i55q96ZrFZ10mOjW-XpMoxSWXeMZbOeIIittoOniRVjlBR2z50cljimMou-XI3_ORk6MoQn6f7kuew4sXuZehxJmSt1XMpQ0no4JrRdIYul505X7dW-mbRz0tSfeYIWea3MW0dVn7yseyQdysynWabfrRfcmVo1I2-XqmJ7zgz1WJcW_5SmlRUtYfuef6O24"
    private var lastFallDetectionTime = 0L
    private val FALL_THRESHOLD = 15.0f // 낙상 감지 임계값
    private val FALL_COOLDOWN = 30000L // 30초 쿨다운
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "FallDetectionChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            if (acceleration > FALL_THRESHOLD) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFallDetectionTime > FALL_COOLDOWN) {
                    lastFallDetectionTime = currentTime
                    detectFall()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 정확도 변경 시 처리
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "낙상 감지 서비스",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "낙상 감지 서비스가 실행 중입니다"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("낙상 감지 서비스")
            .setContentText("낙상 감지 서비스가 실행 중입니다")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun detectFall() {
        // Firebase에서 caregiver의 토큰 가져오기
        val tokenRef = FirebaseDatabase.getInstance()
            .getReference("tokens")
            .child("test1") // caregiver의 UID

        tokenRef.get().addOnSuccessListener { snapshot ->
            val targetToken = snapshot.getValue(String::class.java)
            if (targetToken != null) {
                sendFallAlert(targetToken)
                Log.d("FallDetection", "낙상이 감지되었습니다. 보호자에게 알림을 보냅니다.")
            } else {
                Log.e("FallDetection", "보호자의 토큰을 찾을 수 없습니다.")
            }
        }
    }

    private fun sendFallAlert(toToken: String) {
        val client = OkHttpClient()
        val json = """
        {
          "toToken": "$toToken",
          "title": "낙상 감지",
          "body": "피보호자의 낙상이 감지되었습니다!"
        }
        """.trimIndent()
        val body = json.toRequestBody("application/json; charset=UTF-8".toMediaType())
        val request = Request.Builder()
            .url("http://<서버주소>:<포트>/api/notify/fall") // 예: http://192.168.0.10:3000/api/notify/fall
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FallDetection", "서버로 낙상 이벤트 전송 실패", e)
            }
            override fun onResponse(call: Call, response: Response) {
                Log.d("FallDetection", "서버로 낙상 이벤트 전송 결과: ${response.body?.string()}")
            }
        })
    }
}