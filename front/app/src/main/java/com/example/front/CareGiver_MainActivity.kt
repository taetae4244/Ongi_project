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

    private val accessToken = "ya29.c.c0ASRK0GZaXS7WqwxHDbjnJ0HXZqJ9hP28nzYgGA3mp6fSHOH1sX8BF76_EPZ1xrgM2lA0A1VVQfmeOIO6IaxtWzFihJbAgMaY_7DSbYiQdPXafgZ-RLlcrS5uFI3kEvt6MCZjWJs6mrk3tlCcWqBHgZQt_IvLgbquCSEC3at51n_fD3dxL_uhAUPazGIfbqZwiHAGNpMHfQzVYBBB_4MKSGoG1taPJ9a1UnUFBEynodWpHnRDaD1uACkmQiKQUi93jHNv_lC6v-53CjL9RZeb1YrpW8acH6Vs0lnDJXGzfWBhIxUTTAJi9rXE5wV1XbeIpnTSyU_4TSlBXPeKb3zvB858Ccvyra63B9h1MNuipz9_bIjQ2rG5joQG384PaRBsYgl9eg0MXb_j45puwlrO2JVtu_gl5mkg5OhxxoyI7ja1b8Z-2j5a5_nR7JVFwy6vZgjXeaIW2OccltuQ_OqR5dMB38n5SYthysX-YWkMQ5-RSy7RJhUQ3S7w_evz5m8l9tXxkJRaj5c4SsW3XXplsSyUOsjySish8eV6uskf9yInRwadUe33hRh3cctpWw0Wqmf1vSo1bl2I10p76xk-i9Q85MqqlFgX-QxM-w774Qy8cazUhopi57qxOdaxQsFlyh08dt4tb2YmVxyfRiImy3wv_Rn82QFnjkBVWinz16ehtMyndSJiim8fX8-OIcBthIj8bwmlh5WqoViZuoStws4ar5Zwz50c9y4Ii7V40Fw29QqyyMw9s1I9oWZF-S4u9rIop9WW0_Z18_9vd_lFw9nMi15R4w6Vc-_stwj0zjt-zuXJ6ab25j0Vj5y-Ug5vM-8glWa3lX_2UewR-S-aFeh1iwrgl015pai8v1pn5m2u69Idl14n4wuR9rj6xk5IybpttX7scedu6ieYQ-jwb_cpahmm-2uy_s3nI7sQh-SbmRhIb_qhrakYqXzkwjlciUZl6hkz8SgJv3fUUXobUMYBS8kqQ4t8ki6UhV6fSuxaeSsx3YIfYbF"
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