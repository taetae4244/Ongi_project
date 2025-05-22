package com.example.front

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ConnectionCompleteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_complete)

        val username = intent.getStringExtra("username")

        val confirmButton = findViewById<Button>(R.id.btnConfirm)
        confirmButton.setOnClickListener {
            val intent = Intent(this, CareGiver_MainActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }
    }
}