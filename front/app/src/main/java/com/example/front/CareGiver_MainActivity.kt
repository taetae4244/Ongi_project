package com.example.front

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import android.os.Handler
import android.os.Looper

class CareGiver_MainActivity : AppCompatActivity() {

    private val accessToken = "ya29.c.c0ASRK0GYYfw1Y5IdVwnVqiNiVO6FZJy6wvG4o_2IzR6pvRUQ1LnU5oxtCoL_U-NoZjPMUJuzdbVWcXyPFn7qHf8o7rasj8mOi7p0WQgxRxqaCc3FGpj_QrDPfgAMx8odyVU7JjeegG_mPp-RM0A8MzvVrseh9CES3pRoUTQKU-OOC4uODGHE5bn0F9OqHS1IVQuyjRxVWv-UNjxWuY2gHILCJNT0LT-ImxBHpVP7T5lWg7izk-6rtvAZjKqhERqOdRdFE8CtuGuS_USsG8qPWxoEbaPk7sLBgzWbZw44ON9W5pHMCbtAL4A1ahNhz6jHiREcu-aPAEKWS0rF0jop7vuMd7la5wqr-yUUCByjOFLevOBkX6i_3n3rcL385K7rulMs03qymjWwBud0t32UVc_pZ-c_td-XR2tXBgdbsBxJYQMM2_fVp-8J57QJUvSulJYqwJnq0yJ1tBI6l0qBwJ380IvSOqij47Bh-p4pOx8Srk_dW6Zw-58txWhiW3696wJ__zUS8JO_9i9QIXnZlm6g9Utio-htZYOf-87YFvQ5Vtivh9VB0S_baXgVWwoxr-2VrpkthfWmJumMalQ5Y60QzfMqMibxFeWfcgdvtaiFOB6_nnSzQMeqIXyxjqh2FqgFu113-BR8wMieB0thRU-zna2Iw1UryMl6V5vmugcqYZ6FOxM1JikfqdM0lzWUfprjycJF3xRq4RgnlgpmjzpY7qJx54jzdtnIa0vt8ajyQRFgZFgOst8vbxy3e0a3Rgmmbrwd_JWiuetmi9SQQbcyp3ezQR27ubFnQasFBbcX1-mIFXwyh_b8i55q96ZrFZ10mOjW-XpMoxSWXeMZbOeIIittoOniRVjlBR2z50cljimMou-XI3_ORk6MoQn6f7kuew4sXuZehxJmSt1XMpQ0no4JrRdIYul505X7dW-mbRz0tSfeYIWea3MW0dVn7yseyQdysynWabfrRfcmVo1I2-XqmJ7zgz1WJcW_5SmlRUtYfuef6O24"
    private var isAlertActive = false
    private val handler = Handler(Looper.getMainLooper())
    private var targetToken: String? = null
    private val alertRunnable = object : Runnable {
        override fun run() {
            if (isAlertActive && targetToken != null) {
                sendPushV1(targetToken!!, accessToken)
                handler.postDelayed(this, 2000) // 2초마다 알림 재전송
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caregiver_main)

        val btnCheckLocation = findViewById<FrameLayout>(R.id.btnCheckLocation)
        btnCheckLocation.setOnClickListener {
            val linkedUserUid = intent.getStringExtra("linkedUser")
            if (!linkedUserUid.isNullOrEmpty()) {
                startActivity(Intent(this, GuardianMapActivity::class.java).apply {
                    putExtra("linkedUser", linkedUserUid)
                })
            } else {
                Toast.makeText(this, "연동된 피보호자 계정이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        val btnSendAlert = findViewById<FrameLayout>(R.id.btnsendAlert)
        btnSendAlert.setOnClickListener {
            val targetUid = intent.getStringExtra("linkedUser") ?: "test2"
            val tokenRef = FirebaseDatabase.getInstance()
                .getReference("tokens")
                .child(targetUid)

            tokenRef.get().addOnSuccessListener { snapshot ->
                targetToken = snapshot.getValue(String::class.java)
                if (targetToken != null) {
                    if (!isAlertActive) {
                        // 알림 시작
                        isAlertActive = true
                        sendPushV1(targetToken!!, accessToken)
                        handler.postDelayed(alertRunnable, 2000)
                        Toast.makeText(this, "긴급 알림이 시작되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 알림 중지
                        isAlertActive = false
                        handler.removeCallbacks(alertRunnable)
                        Toast.makeText(this, "긴급 알림이 중지되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "피보호자의 토큰이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isAlertActive = false
        handler.removeCallbacks(alertRunnable)
    }

    private fun sendPushV1(toToken: String, accessToken: String) {
        val client = OkHttpClient()

        val json = """
        {
          "message": {
            "token": "$toToken",
            "notification": {
              "title": "긴급 호출",
              "body": "보호자가 긴급 호출을 보냈습니다!"
            },
            "android": {
              "priority": "HIGH",
              "notification": {
                "sound": "siren",
                "channel_id": "emergency_channel",
                "visibility": "public",
                "vibrate_timings": ["0.1s", "0.1s", "0.1s", "0.1s", "0.1s"]
              }
            },
            "apns": {
              "payload": {
                "aps": {
                  "sound": "siren.mp3",
                  "badge": 1,
                  "content-available": 1
                }
              },
              "headers": {
                "apns-priority": "10"
              }
            }
          }
        }
        """.trimIndent()

        val body = json.toRequestBody("application/json; charset=UTF-8".toMediaType())
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/v1/projects/carenect-app/messages:send")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCMv1", "전송 실패", e)
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    Log.d("FCMv1", "전송 결과: ${it.body?.string()}")
                }
            }
        })
    }
}