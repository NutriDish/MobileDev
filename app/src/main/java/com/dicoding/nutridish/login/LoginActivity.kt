package com.dicoding.nutridish.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.ActivityLoginBinding
import com.dicoding.nutridish.main.MainActivity
import com.dicoding.nutridish.main.MainViewModel
import com.dicoding.nutridish.view.HomeActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel
        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        // Setup click listener
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString() // Gunakan binding untuk referensi
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
                observeLoginResponse()
            } else {
                Toast.makeText(this, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeLoginResponse() {
        viewModel.loginResponse.observe(this) { response ->
            if (response != null && !response.isError) {
                Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                // Pindah ke halaman utama
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, response?.getMessage() ?: "Login Gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}