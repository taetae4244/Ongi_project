package com.example.front

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val idEditText = findViewById<EditText>(R.id.editTextId)
        val pwEditText = findViewById<EditText>(R.id.editTextPassword)
        val pwConfirmEditText = findViewById<EditText>(R.id.editTextPasswordConfirm)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val signupBtn = findViewById<Button>(R.id.btnSignup)

        signupBtn.setOnClickListener {
            val username = idEditText.text.toString()
            val password = pwEditText.text.toString()
            val passwordConfirm = pwConfirmEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val guardianRadio = findViewById<RadioButton>(R.id.radioGuardian)
            val seniorRadio = findViewById<RadioButton>(R.id.radioSenior)

            val role = when {
                guardianRadio.isChecked -> "guardian"
                seniorRadio.isChecked -> "senior"
                else -> {
                    Toast.makeText(this, "역할을 선택해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            val request = SignupRequest(username, password, passwordConfirm, email, phone, role)


            if (password != passwordConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitInstance.api.signup(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SignupActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()

                        // ✅ 로그인 화면으로 명시적으로 이동
                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignupActivity, "회원가입 실패: 중복된 아이디일 수 있어요.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@SignupActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
