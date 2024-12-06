package com.dicoding.nutridish.login

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.ActivityLoginBinding
import com.dicoding.nutridish.personalization.PersonalizeActivity
import com.dicoding.nutridish.signup.SignUpActivity
import com.dicoding.nutridish.view.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore: FirebaseFirestore

    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this) // Make sure ViewModelFactory is correctly set up
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textView = findViewById<TextView>(R.id.messageTextView)

        val text = getString(R.string.message_login_page)

        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Pindah ke halaman RegisterActivity
                val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
        }

        // Menentukan kata "Register" agar bisa diklik
        val startIndex = text.indexOf("account")
        val endIndex = startIndex + "account".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance() // Untuk mengaktifkan klik


        // Inisialisasi Firebase Auth dan Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Login dengan Email
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmail(email, password)
            } else {
                Toast.makeText(this, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        lifecycleScope.launch {
                            checkUserData(user.uid)
                            viewModel.saveSession(email,password,true)
                        }

                    } else {
                        Toast.makeText(this, "Email belum diverifikasi. Silakan periksa email Anda.", Toast.LENGTH_SHORT).show()
                        auth.signOut() // Logout user jika email belum diverifikasi
                    }
                } else {
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserData(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val weight = document.getLong("weight")?.toInt()
                    val age = document.getLong("age")?.toInt()

                    if (weight == 0 || age == 0) {
                        // Data personalisasi belum lengkap, arahkan ke PersonalizeActivity
                        startActivity(Intent(this, PersonalizeActivity::class.java))
                    } else {
                        // Data lengkap, lanjutkan ke HomeActivity
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finish()
                } else {
                    // Data pengguna belum ada, buat data baru di Firestore
                    val userData = mapOf(
                        "name" to (auth.currentUser?.displayName ?: "User"), // Gunakan displayName dari FirebaseAuth
                        "email" to (auth.currentUser?.email ?: "Email tidak ditemukan"),
                        "weight" to 0,
                        "age" to 0,
                        "tags" to mapOf(
                            "pork" to false,
                            "alcohol" to false
                        )
                    )

                    firestore.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, PersonalizeActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memeriksa data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}