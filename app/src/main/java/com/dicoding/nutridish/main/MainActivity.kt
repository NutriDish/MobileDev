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
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.ActivityMainBinding
import com.dicoding.nutridish.home.HomeActivity
import com.dicoding.nutridish.signup.SignUpActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this) // Make sure ViewModelFactory is correctly set up
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()

        // Observing user session
        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish() // Close the current activity
            }
        }


        val textView = findViewById<TextView>(R.id.textLogin)
        val text = "Belum punya akun? Register"
        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Pindah ke halaman RegisterActivity
                val intent = Intent(this@MainActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
        }

        // Menentukan kata "Register" agar bisa diklik
        val startIndex = text.indexOf("Register")
        val endIndex = startIndex + "Register".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance() // Untuk mengaktifkan klik


//        // Navigating to SignUpActivity when register button is clicked
//        binding.registerButton.setOnClickListener {
//            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
//            startActivity(intent)
//        }
    }


    // Handle window appearance (hide status bar)
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
        supportActionBar?.hide() // Hide the action bar
    }
}
