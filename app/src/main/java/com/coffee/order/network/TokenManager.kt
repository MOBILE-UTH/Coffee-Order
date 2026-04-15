package com.coffee.order.network

import android.content.Context
import android.content.SharedPreferences
import com.coffee.shared.UserRole
import androidx.core.content.edit

/**
 * Manages JWT token and user session data using SharedPreferences.
 */
object TokenManager {
    private const val PREFS_NAME = "coffee_order_auth"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_ROLE = "role"
    private const val KEY_SERVER_URL = "server_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(
        token: String, userId: Long, username: String, displayName: String, role: UserRole
    ) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_DISPLAY_NAME, displayName)
            putString(KEY_ROLE, role.name)
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getDisplayName(): String = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""

    fun getRole(): UserRole? {
        val roleStr = prefs.getString(KEY_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun isOwner(): Boolean = getRole() == UserRole.OWNER

    fun getServerUrl(): String =
        prefs.getString(KEY_SERVER_URL, "http://192.168.10.72:8080") ?: "http://192.168.10.72:8080"

    fun setServerUrl(url: String) {
        prefs.edit { putString(KEY_SERVER_URL, url) }
    }

    fun clearSession() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_DISPLAY_NAME)
            remove(KEY_ROLE)
        }
    }
}
