package com.example.front

import com.example.front.models.LinkRequest
import com.example.front.models.UserResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/api/signup")
    fun signup(@Body request: SignupRequest): Call<LoginResponse>

    @POST("/api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/link")  // ✅ 추가: 사용자 연동 요청
    fun linkUsers(@Body request: LinkRequest): Call<ResponseBody>

    @GET("/api/user/{username}")
    fun getUserInfo(@Path("username") username: String): Call<UserResponse>

}