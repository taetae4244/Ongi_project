package com.example.front

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "UserPrefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ROLE = "role"
        private const val KEY_AUTO_LOGIN = "auto_login"
        private const val KEY_LINKED_USER = "linked_user"
    }

    fun saveLoginInfo(username: String, password: String, role: String, linkedUser: String? = null) {
        sharedPreferences.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
            putString(KEY_ROLE, role)
            putString(KEY_LINKED_USER, linkedUser)
            putBoolean(KEY_AUTO_LOGIN, true)
            apply()
        }
    }

    fun clearLoginInfo() {
        sharedPreferences.edit().clear().apply()
    }

    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)
    fun getPassword(): String? = sharedPreferences.getString(KEY_PASSWORD, null)
    fun getRole(): String? = sharedPreferences.getString(KEY_ROLE, null)
    fun getLinkedUser(): String? = sharedPreferences.getString(KEY_LINKED_USER, null)
    fun isAutoLoginEnabled(): Boolean = sharedPreferences.getBoolean(KEY_AUTO_LOGIN, false)
} 