package com.example.front

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.front.RetrofitInstance.api
import com.example.front.models.LoginRequest
import com.example.front.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.front.models.UserResponse


class SplashActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager

    private fun animateTyping(textView: TextView, text: String, delay: Long = 100L) {
        var index = 0
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                if (index <= text.length) {
                    textView.text = text.substring(0, index)
                    index++
                    handler.postDelayed(this, delay)
                }
            }
        }
        handler.post(runnable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preferenceManager = PreferenceManager(this)

        val logo = findViewById<ImageView>(R.id.logo)
        val loadingText = findViewById<TextView>(R.id.loadingText)
        val splashRoot = findViewById<View>(R.id.splashRoot)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(fadeIn)

        animateTyping(loadingText, "로딩중...", 150)

        Handler(Looper.getMainLooper()).postDelayed({
            val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            splashRoot.startAnimation(fadeOut)

            Handler(Looper.getMainLooper()).postDelayed({
                if (preferenceManager.isAutoLoginEnabled()) {
                    performAutoLogin()
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finish()
                }
            }, 500)
        }, 2000)
    }

    private fun performAutoLogin() {
        val username = preferenceManager.getUsername()
        val password = preferenceManager.getPassword()
        
        if (username != null && password != null) {
            val loginRequest = LoginRequest(username, password)
            api.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val role = loginResponse?.role
                        
                        when (role) {
                            "guardian" -> {
                                api.getUserInfo(username).enqueue(object : Callback<UserResponse> {
                                    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                                        val user = response.body()
                                        val intent = if (user?.linkedUser != null) {
                                            Intent(this@SplashActivity, CareGiver_MainActivity::class.java)
                                                .putExtra("linkedUser", user.linkedUser)
                                        } else {
                                            Intent(this@SplashActivity, CareGiver_HomeActivity::class.java)
                                        }
                                        intent.putExtra("username", username)
                                        startActivity(intent)
                                        finish()
                                    }

                                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                        finish()
                                    }
                                })
                            }
                            "senior" -> {
                                api.getUserInfo(username).enqueue(object : Callback<UserResponse> {
                                    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                                        val user = response.body()
                                        if (user?.linkedUser != null) {
                                            val intent = Intent(this@SplashActivity, SeniorEmergencyCallActivity::class.java)
                                            intent.putExtra("username", username)
                                            intent.putExtra("linkedUser", user.linkedUser)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                            finish()
                                        }
                                    }

                                    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                        finish()
                                    }
                                })
                            }
                            else -> {
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                    } else {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            })
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
