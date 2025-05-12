package com.example.front

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class Senior_HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection) // ← 연결 화면 UI로 설정

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        btnConnect.setOnClickListener {
            val intent = Intent(this, ConnectionCompleteActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
