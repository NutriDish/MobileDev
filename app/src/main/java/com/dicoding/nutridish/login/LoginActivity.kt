package com.dicoding.nutridish.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.nutridish.databinding.ActivityLoginBinding
import com.dicoding.nutridish.personalization.PersonalizeActivity
import com.dicoding.nutridish.view.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                        checkUserData(user.uid)
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
