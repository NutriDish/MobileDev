package com.dicoding.nutridish.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.ActivityLoginBinding
import com.dicoding.nutridish.databinding.ActivityMainBinding
import com.dicoding.nutridish.login.LoginActivity
import com.dicoding.nutridish.personalization.PersonalizeActivity
import com.dicoding.nutridish.view.HomeActivity
import com.dicoding.nutridish.signup.SignUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this) // Make sure ViewModelFactory is correctly set up
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Observing user session
        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                auth.signInWithEmailAndPassword(user.email, user.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val users = auth.currentUser
                            if (users != null) {
                                firestore.collection("users").document(users.uid).get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val weight = document.getLong("weight")?.toInt()
                                            val age = document.getLong("age")?.toInt()

                                            if (weight == 0 || age == 0) {
                                                // Data personalisasi belum lengkap, arahkan ke PersonalizeActivity
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        PersonalizeActivity::class.java
                                                    )
                                                )
                                            } else {
                                                // Data lengkap, lanjutkan ke HomeActivity
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        HomeActivity::class.java
                                                    )
                                                )
                                            }
                                            finish()
                                        }
                                    }
                            } else {
                                lifecycleScope.launch {
                                    viewModel.logout() // Logout user
                                }
                            }
                        }
                    }
                finish() // Close the current activity
            }
        }


        val textView = findViewById<TextView>(R.id.textLogin)

        val text = getString(R.string.already_have_account)

        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Pindah ke halaman RegisterActivity
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        // Menentukan kata "Register" agar bisa diklik
        val startIndex = text.indexOf("Login")
        val endIndex = startIndex + "Login".length
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance() // Untuk mengaktifkan klik


        // Navigating to SignUpActivity when register button is clicked
        binding.registerButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }


    // Handle window appearance (hide status bar)
    fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide() // Hide the action bar
    }
}
