package com.example.front

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.front.RetrofitInstance.api
import com.example.front.LoginRequest
import com.example.front.LoginResponse
import com.example.front.models.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

class MainActivity : AppCompatActivity() {
    private val PREFS_NAME = "UserPrefs"
    private val KEY_USERNAME = "username"

    private fun refreshFCMToken() {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                // 토큰 삭제 성공 후 새 토큰 발급
                FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                    if (tokenTask.isSuccessful) {
                        Log.d("FCM", "새 토큰이 발급되었습니다.")
                    } else {
                        Log.e("FCM", "새 토큰 발급 실패", tokenTask.exception)
                    }
                }
            } else {
                Log.e("FCM", "토큰 삭제 실패", deleteTask.exception)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val registerButton = findViewById<Button>(R.id.registerLink)
        registerButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        val loginButton = findViewById<Button>(R.id.loginButton)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val loginRequest = LoginRequest(username, password)

            api.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val role = loginResponse?.role

                        // 로그인 성공 시 사용자 정보를 SharedPreferences에 저장
                        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        prefs.edit().putString(KEY_USERNAME, username).apply()

                        // FCM 토큰 갱신
                        refreshFCMToken()

                        Toast.makeText(this@MainActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()

                        when (role) {
                            "guardian" -> {
                                api.getUserInfo(username).enqueue(object : Callback<UserResponse> {
                                    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                                        val user = response.body()
                                        val intent = if (user?.linkedUser != null) {
                                            Intent(this@MainActivity, CareGiver_MainActivity::class.java)
                                                .putExtra("linkedUser", user.linkedUser) // ✅ 연동된 피보호자 UID 전달
                                        } else {
                                            Intent(this@MainActivity, CareGiver_HomeActivity::class.java)
                                        }
                                        intent.putExtra("username", username)
                                        startActivity(intent)
                                        finish()
                                    }

                                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                                        Toast.makeText(this@MainActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }


                            "senior" -> {
                                api.getUserInfo(username).enqueue(object : Callback<UserResponse> {
                                    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                                        val user = response.body()
                                        if (user?.linkedUser != null) {
                                            // ✅ 수정된 부분: username 값을 uid로 사용하여 전달
                                            val intent = Intent(this@MainActivity, SeniorEmergencyCallActivity::class.java)
                                            intent.putExtra("username", username)
                                            intent.putExtra("linkedUser", user?.linkedUser)
                                            startActivity(intent)
                                            finish()

                                        } else {
                                            Toast.makeText(this@MainActivity, "아직 연동되지 않은 계정입니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                                        Toast.makeText(this@MainActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }

                            else -> {
                                Toast.makeText(this@MainActivity, "알 수 없는 사용자 역할입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        Toast.makeText(this@MainActivity, "아이디 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
