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

class SeniorEmergencyCallActivity : AppCompatActivity() {

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var uid: String
    private lateinit var locationRequest: LocationRequest
    private val handler = Handler(Looper.getMainLooper())

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
        }

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
