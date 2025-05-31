package com.example.front

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.front.models.LinkRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.front.RetrofitInstance

// 보호자, 피보호자 계정 생성 후, 생성한 보호자 계정으로 첫 로그인 시 피보호자 계정과 연동하는 코드
class CareGiver_HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val editTextTargetUsername = findViewById<EditText>(R.id.editTextTargetUsername) // 연결 대상 입력창

        btnConnect.setOnClickListener {
            val currentUsername = intent.getStringExtra("username") ?: ""
            val targetUsername = editTextTargetUsername.text.toString()

            if (targetUsername.isBlank()) {
                Toast.makeText(this, "상대방 아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val linkRequest = LinkRequest(currentUsername, targetUsername)

            RetrofitInstance.api.linkUsers(linkRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CareGiver_HomeActivity, "연동 성공!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CareGiver_HomeActivity, ConnectionCompleteActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@CareGiver_HomeActivity, "연동 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@CareGiver_HomeActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
