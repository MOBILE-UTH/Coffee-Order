package com.coffee.order.feature.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.coffee.order.R
import com.coffee.order.databinding.ActivityLoginBinding
import com.coffee.order.feature.admin.AdminActivity
import com.coffee.order.feature.employee.EmployeeActivity
import com.coffee.order.network.TokenManager
import com.coffee.order.repository.CoffeeOrderRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val coffeeOrderRepository = CoffeeOrderRepository()
    private lateinit var credentialManager: CredentialManager
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenManager.init(applicationContext)
        credentialManager = CredentialManager.create(this)

        // Nếu đã đăng nhập, bỏ qua màn login
        if (TokenManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load server URL đã lưu
        binding.editTextServer.setText(TokenManager.getServerUrl())

        // Tự động điền thông tin đã lưu từ Google Password Manager
        autoFillSavedCredentials()

        // Password visibility toggle
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.editTextPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            } else {
                binding.editTextPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            }
            binding.editTextPassword.setSelection(binding.editTextPassword.text?.length ?: 0)
        }

        binding.buttonLogin.setOnClickListener {
            performLogin()
        }
    }

    /**
     * Thử lấy thông tin đã lưu từ Google Password Manager.
     * Nếu có → tự động điền vào form.
     * Nếu không → không làm gì, user tự nhập.
     */
    private fun autoFillSavedCredentials() {
        lifecycleScope.launch {
            try {
                val request = GetCredentialRequest(
                    credentialOptions = listOf(GetPasswordOption())
                )
                val result = credentialManager.getCredential(this@LoginActivity, request)
                val credential = result.credential

                if (credential is PasswordCredential) {
                    binding.editTextUsername.setText(credential.id)
                    binding.editTextPassword.setText(credential.password)
                }
            } catch (_: NoCredentialException) {
                // Chưa có thông tin nào đã lưu — bình thường, không cần xử lý
            } catch (e: GetCredentialException) {
                Log.d("LoginActivity", "Credential autofill skipped: ${e.message}")
            }
        }
    }

    /**
     * Sau khi đăng nhập thành công, đề xuất Google lưu thông tin tài khoản.
     */
    private fun saveCredentials(username: String, password: String) {
        lifecycleScope.launch {
            try {
                credentialManager.createCredential(
                    context = this@LoginActivity,
                    request = CreatePasswordRequest(id = username, password = password)
                )
            } catch (e: Exception) {
                // Người dùng từ chối lưu hoặc không dùng Google Account — bỏ qua
                Log.d("LoginActivity", "Credential save skipped: ${e.message}")
            }
        }
    }

    private fun performLogin() {
        val serverUrl = binding.editTextServer.text?.toString()?.trim() ?: ""
        val username = binding.editTextUsername.text?.toString()?.trim() ?: ""
        val password = binding.editTextPassword.text?.toString()?.trim() ?: ""

        // Validation
        if (serverUrl.isBlank()) {
            showError(getString(R.string.please_enter_server_address))
            return
        }
        if (username.isBlank()) {
            showError(getString(R.string.please_enter_username))
            return
        }
        if (password.isBlank()) {
            showError(getString(R.string.please_enter_password))
            return
        }

        // Lưu server URL
        TokenManager.setServerUrl(serverUrl)

        setLoading(true)
        hideError()

        lifecycleScope.launch {
            val result = coffeeOrderRepository.login(username, password)
            setLoading(false)

            result.onSuccess {
                // Đề xuất lưu credentials sau khi đăng nhập thành công
                saveCredentials(username, password)
                navigateToMain()
            }.onFailure { e ->
                showError("Đăng nhập thất bại: ${e.localizedMessage ?: "Lỗi kết nối"}")
            }
        }
    }

    private fun navigateToMain() {
        val intent = if (TokenManager.isOwner()) {
            Intent(this, AdminActivity::class.java)
        } else {
            Intent(this, EmployeeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !loading
        binding.buttonLogin.alpha = if (loading) 0.7f else 1.0f
    }

    private fun showError(message: String) {
        binding.textViewError.text = message
        binding.textViewError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.textViewError.visibility = View.GONE
    }
}
