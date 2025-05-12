package com.example.front

data class SignupRequest(
    val username: String,
    val password: String,
    val password_confirm: String,
    val email: String,
    val phone: String,
    val role: String       // ← guardian 또는 senior
)

