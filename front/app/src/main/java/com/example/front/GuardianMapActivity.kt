package com.example.front

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class GuardianMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var database: DatabaseReference
    private var marker: Marker? = null
    private var linkedUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian_map)

        linkedUsername = intent.getStringExtra("linkedUser")

        if (linkedUsername == null) {
            Log.e("GuardianMap", "연동된 사용자 정보가 없습니다.")
            finish()
            return
        }

        // ✅ 수정: 멤버 변수 database를 정확히 초기화
        database = FirebaseDatabase.getInstance().getReference("locations/$linkedUsername")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment  // ✅ ID 일치

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true  // ✅ 줌 버튼 표시

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lng = snapshot.child("longitude").getValue(Double::class.java)

                Log.d("GuardianMap", "수신된 위치: lat=$lat, lng=$lng")  // ✅ 확인용 로그

                if (lat != null && lng != null) {
                    val location = LatLng(lat, lng)

                    if (marker == null) {
                        marker = googleMap.addMarker(
                            MarkerOptions().position(location).title("피보호자 위치")
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))
                    } else {
                        marker?.position = location
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                    }
                } else {
                    Log.w("GuardianMap", "위치 정보가 null입니다.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GuardianMap", "Firebase 읽기 실패: ${error.message}")
            }
        })
    }

}
