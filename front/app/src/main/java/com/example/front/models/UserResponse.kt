package com.example.front.models

data class UserResponse (
    val username: String,
    val email: String,
    val phone: String,
    val role: String,
    val linkedUser: String? // ← 연동 정보가 없으면 null
)