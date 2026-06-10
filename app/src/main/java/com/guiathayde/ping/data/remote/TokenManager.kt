package com.guiathayde.ping.data.remote

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ping_prefs", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("jwt_token", null)
        set(value) = prefs.edit().putString("jwt_token", value).apply()

    var userId: String?
        get() = prefs.getString("user_id", null)
        set(value) = prefs.edit().putString("user_id", value).apply()

    var displayName: String?
        get() = prefs.getString("display_name", null)
        set(value) = prefs.edit().putString("display_name", value).apply()

    var username: String?
        get() = prefs.getString("username", null)
        set(value) = prefs.edit().putString("username", value).apply()

    val isLoggedIn: Boolean
        get() = token != null

    fun clear() {
        prefs.edit().clear().apply()
    }
}
