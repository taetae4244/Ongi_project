package com.example.front

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.front.RetrofitInstance.api
import com.example.front.models.LoginRequest
import com.example.front.models.LoginResponse
import com.example.front.models.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        preferenceManager = PreferenceManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val registerButton = findViewById<Button>(R.id.registerLink)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val autoLoginCheckBox = findViewById<CheckBox>(R.id.autoLoginCheckBox)

        // manage.kt의 공통 함수로 UI 제어 및 기능 위임
        com.example.front.Manage.setRegisterButton(this, registerButton)
        com.example.front.Manage.setLoginButton(
            this,
            loginButton,
            usernameEditText,
            passwordEditText,
            autoLoginCheckBox,
            preferenceManager
        )
    }
}
