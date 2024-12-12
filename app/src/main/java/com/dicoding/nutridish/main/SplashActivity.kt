package com.dicoding.nutridish.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.ActivitySplashBinding
import com.dicoding.nutridish.personalization.PersonalizeActivity
import com.dicoding.nutridish.view.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setupView()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                auth.signInWithEmailAndPassword(user.email, user.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                firestore.collection("users").document(currentUser.uid).get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val weight = document.getLong("weight")?.toInt() ?: 0
                                            val age = document.getLong("age")?.toInt() ?: 0

                                            val targetActivity = if (weight == 0 || age == 0) {
                                                PersonalizeActivity::class.java
                                            } else {
                                                HomeActivity::class.java
                                            }

                                            startActivity(
                                                Intent(this, targetActivity).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                            )
                                            finish()
                                        }
                                    }
                                    .addOnFailureListener {
                                        handleLoginFailure()
                                    }
                            }
                        } else {
                            handleLoginFailure()
                        }
                    }
            } else {
                lifecycleScope.launch {
                    delay(2000L)
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun handleLoginFailure() {
        lifecycleScope.launch {
            viewModel.logout()
            showToast("Authentication failed.")
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}