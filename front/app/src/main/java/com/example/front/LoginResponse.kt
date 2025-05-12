package com.example.front

data class LoginResponse (
    val message: String,  // 예: "로그인 성공"
    val token: String,
    val role: String      // 예: "guardian" 또는 "senior"
)