// manage.kt
package com.example.front

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.*
import com.example.front.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object Manage {
    // 회원가입 이동
    fun setRegisterButton(activity: Activity, button: Button) {
        button.setOnClickListener {
            activity.startActivity(Intent(activity, SignupActivity::class.java))
        }
    }

    // 로그인 처리
    fun setLoginButton(
        activity: Activity,
        button: Button,
        usernameEditText: EditText,
        passwordEditText: EditText,
        autoLoginCheckBox: CheckBox?,
        preferenceManager: PreferenceManager
    ) {
        button.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(activity, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val loginRequest = LoginRequest(username, password)
            RetrofitInstance.api.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val role = loginResponse?.role
                        Toast.makeText(activity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        // 역할별 분기 및 자동로그인 처리
                        when (role) {
                            "guardian" -> {
                                getUserInfoAndMove(
                                    activity, username, role, autoLoginCheckBox, preferenceManager,
                                    CareGiver_MainActivity::class.java, CareGiver_HomeActivity::class.java
                                )
                            }
                            "senior" -> {
                                getUserInfoAndMove(
                                    activity, username, role, autoLoginCheckBox, preferenceManager,
                                    SeniorEmergencyCallActivity::class.java, null
                                )
                            }
                            else -> {
                                Toast.makeText(activity, "알 수 없는 사용자 역할입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(activity, "아이디 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(activity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // 회원가입 처리
    fun setSignupButton(
        activity: Activity,
        button: Button,
        idEditText: EditText,
        pwEditText: EditText,
        pwConfirmEditText: EditText,
        emailEditText: EditText,
        phoneEditText: EditText,
        guardianRadio: RadioButton,
        seniorRadio: RadioButton
    ) {
        button.setOnClickListener {
            val username = idEditText.text.toString()
            val password = pwEditText.text.toString()
            val passwordConfirm = pwConfirmEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val role = when {
                guardianRadio.isChecked -> "guardian"
                seniorRadio.isChecked -> "senior"
                else -> {
                    Toast.makeText(activity, "역할을 선택해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            if (password != passwordConfirm) {
                Toast.makeText(activity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val request = SignupRequest(username, password, passwordConfirm, email, phone, role)
            RetrofitInstance.api.signup(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(activity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                    } else {
                        Toast.makeText(activity, "회원가입 실패: 중복된 아이디일 수 있어요.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(activity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // 연동 처리
    fun setLinkButton(
        activity: Activity,
        button: Button,
        currentUsername: String,
        targetEditText: EditText
    ) {
        button.setOnClickListener {
            val targetUsername = targetEditText.text.toString()
            if (targetUsername.isBlank()) {
                Toast.makeText(activity, "상대방 아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val linkRequest = LinkRequest(currentUsername, targetUsername)
            RetrofitInstance.api.linkUsers(linkRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(activity, "연동 성공!", Toast.LENGTH_SHORT).show()
                        activity.startActivity(Intent(activity, ConnectionCompleteActivity::class.java))
                        activity.finish()
                    } else {
                        Toast.makeText(activity, "연동 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(activity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // 사용자 정보 조회 및 역할별 화면 이동
    private fun getUserInfoAndMove(
        activity: Activity,
        username: String,
        role: String?,
        autoLoginCheckBox: CheckBox?,
        preferenceManager: PreferenceManager,
        mainActivityClass: Class<out Activity>,
        homeActivityClass: Class<out Activity>?
    ) {
        RetrofitInstance.api.getUserInfo(username).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val user = response.body()
                val intent = if (role == "guardian" && user?.linkedUser != null && mainActivityClass != null) {
                    Intent(activity, mainActivityClass).putExtra("linkedUser", user.linkedUser)
                } else if (role == "guardian" && homeActivityClass != null) {
                    Intent(activity, homeActivityClass)
                } else if (role == "senior" && user?.linkedUser != null && mainActivityClass != null) {
                    Intent(activity, mainActivityClass)
                        .putExtra("username", username)
                        .putExtra("linkedUser", user.linkedUser)
                } else {
                    null
                }
                intent?.putExtra("username", username)
                if (autoLoginCheckBox?.isChecked == true) {
                    preferenceManager.saveLoginInfo(username, "", role ?: "", user?.linkedUser)
                } else {
                    preferenceManager.clearLoginInfo()
                }
                if (intent != null) {
                    activity.startActivity(intent)
                    activity.finish()
                } else {
                    Toast.makeText(activity, "아직 연동되지 않은 계정입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(activity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 기타 공통 함수(토스트, 화면 이동 등)도 필요시 추가
}