package com.guiathayde.ping.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("display_name") val displayName: String,
    val username: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    @SerializedName("display_name") val displayName: String,
    val username: String
)
