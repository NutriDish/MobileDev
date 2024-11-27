package com.dicoding.nutridish.signup

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.ActivitySignUpBinding
import com.dicoding.nutridish.main.MainViewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showMessage("Oops!", "Please fill in all fields.")
                return@setOnClickListener
            }

            if (password.length >= 8) {
                // Call the register function from the ViewModel
                viewModel.register(name, email, password)

                // Observe registration result
                viewModel.registerResponse.observe(this) { response ->
                    if (!response.isError) {
                        // Registration successful
                        showMessage("Yeah!", "Account with $email has been created. Please log in to see your stories.")
                    } else {
                        // Show error message
                        showMessage("Oops!", response.message ?: "Registration failed")
                    }
                }
            } else {
                showMessage("Oops!", "Password must be at least 8 characters long.")
            }
        }
    }

    private fun showMessage(title: String, message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { _, _ ->
                if (title == "Yeah!") {
                    finish() // Close the registration activity after success
                }
            }
            create()
            show()
        }
    }
}
