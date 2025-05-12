package com.example.front

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SeniorEmergencyCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_call)

        val emergencyButton = findViewById<LinearLayout>(R.id.btnEmergency)

        emergencyButton.setOnClickListener {
            // 실제 긴급 호출 기능이 아직 없다면 토스트로 대체
            Toast.makeText(this, "긴급 호출 신호 전송 중...", Toast.LENGTH_SHORT).show()
        }
    }
}