package com.coffee.order.feature.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.coffee.order.feature.admin.graph.AdminMainScreen
import com.coffee.order.theme.CoffeeAdminTheme
import com.coffee.order.feature.auth.LoginActivity
import com.coffee.order.network.TokenManager

/**
 * Màn hình chính dành cho Quản trị (OWNER).
 * Sử dụng 100% Jetpack Compose.
 */
class AdminActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenManager.init(applicationContext)

        if (!TokenManager.isLoggedIn() || !TokenManager.isOwner()) {
            navigateToLogin()
            return
        }

        setContent {
            CoffeeAdminTheme {
                AdminMainScreen(onLogout = { logout() })
            }
        }
    }

    private fun logout() {
        TokenManager.clearSession()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}