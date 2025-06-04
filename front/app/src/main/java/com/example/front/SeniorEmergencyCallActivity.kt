package com.example.front

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

// 자이로 센서 코드
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody

// 자이로센서 관련 추가 import
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class SeniorEmergencyCallActivity : AppCompatActivity(),SensorEventListener {

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var uid: String
    private lateinit var locationRequest: LocationRequest
    private val handler = Handler(Looper.getMainLooper())

    // 자이로 센서 코드
    private lateinit var sensorManager: SensorManager
    private var gyroSensor: Sensor? = null
    private var isAlertSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_call)

        uid = intent.getStringExtra("username") ?: ""

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 30000
            fastestInterval = 10000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null && uid.isNotEmpty()) {
                    val data = mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude
                    )
                    FirebaseDatabase.getInstance()
                        .getReference("locations/$uid")
                        .setValue(data)

                    Log.d("SeniorLocation", "전송됨: ${location.latitude}, ${location.longitude}")
                    Toast.makeText(
                        this@SeniorEmergencyCallActivity,
                        "위치 전송됨\n위도: ${location.latitude}, 경도: ${location.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.w("SeniorLocation", "실시간 위치도 null")
                    Toast.makeText(this@SeniorEmergencyCallActivity, "실시간 위치 수신 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
        } else {
            startRequestingLocation()
        }

        // 자이로 센서 코드
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    // 자이로 센서 코드
    override fun onResume() {
        super.onResume()
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    // 자이로 센서 코드
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    // 자이로 센서 코드
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val threshold = 1.0f // 적절한 임계값(상황 따라 조정)
            if (!isAlertSent && (Math.abs(x) > threshold || Math.abs(y) > threshold || Math.abs(z) > threshold)) {
                //isAlertSent = true // 중복 전송 방지
                sendGyroAlertToServer(x, y, z)
                Toast.makeText(this, "급격한 움직임 감지, 서버로 알림 전송!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun sendGyroAlertToServer(x: Float, y: Float, z: Float) {
        val url = "https://ongi-project.onrender.com/api/gyro-alert" // 실제 엔드포인트로 수정
        val client = OkHttpClient()
        val json = """
        {
            "uid": "$uid",
            "gyro_x": $x,
            "gyro_y": $y,
            "gyro_z": $z,
            "timestamp": ${System.currentTimeMillis()}
        }
    """.trimIndent()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GyroAlert", "서버 전송 실패: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                Log.d("GyroAlert", "서버 응답: ${response.body?.string()}")
            }
        })
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRequestingLocation()
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRequestingLocation() {
        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        locationClient.removeLocationUpdates(locationCallback)
    }
}
