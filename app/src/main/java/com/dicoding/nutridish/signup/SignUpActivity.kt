package com.dicoding.nutridish.signup

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.nutridish.R
import com.dicoding.nutridish.databinding.ActivitySignUpBinding
import com.dicoding.nutridish.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.app.DatePickerDialog
import java.util.Calendar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Locale

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textView = findViewById<TextView>(R.id.messageTextView)

        val text = getString(R.string.message_signup_page)

        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        val startIndex = text.indexOf("Sign In")
        val endIndex = startIndex + "Sign In".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance()

        binding.dateEditTextLayout.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                    binding.dateEditTextLayout.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.passwordconfirmEditText.text.toString()

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password harus minimal 6 karakter", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(name, email, password)
            }
        }
    }

    private fun getNextUserId(callback: (Long?) -> Unit) {
        val counterDocRef = firestore.collection("counters").document("userIdCounter")

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterDocRef)
            val currentId = snapshot.getLong("value") ?: 0L
            val newId = currentId + 1
            transaction.update(counterDocRef, "value", newId)
            newId
        }.addOnSuccessListener { newId ->
            callback(newId)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Gagal mendapatkan ID pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        val dateOfBirthInput = binding.dateEditTextLayout.text.toString()

        if (TextUtils.isEmpty(dateOfBirthInput)) {
            Toast.makeText(this, "Tanggal lahir harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateFormatReg = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val dateOfBirthFormatted: String
        try {
            val dateOfBirthDate = dateFormatInput.parse(dateOfBirthInput)
            dateOfBirthFormatted = dateFormatOutput.format(dateOfBirthDate!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Format tanggal lahir tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val dateRegFormatted = dateFormatReg.format(System.currentTimeMillis())

        // Perhitungan usia
        val dateParts = dateOfBirthInput.split("/")
        val day = dateParts[0].toInt()
        val month = dateParts[1].toInt() - 1
        val year = dateParts[2].toInt()

        val dobCalendar = Calendar.getInstance().apply {
            set(year, month, day)
        }

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age -= 1
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            getNextUserId { autoIncrementId ->
                                if (autoIncrementId != null) {
                                    val userId = user.uid
                                    val userData = mapOf(
                                        "userId" to autoIncrementId,
                                        "userName" to name,
                                        "email" to email,
                                        "dateBirth" to dateOfBirthFormatted,
                                        "dateReg" to dateRegFormatted,
                                        "age" to age,
                                        "weight" to 0,
                                        "temp" to null,
                                        "loc" to null,
                                        "cons_alkohol" to 0,
                                        "cons_pork" to 0,
                                        "password" to 0
                                    )

                                    firestore.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this,
                                                "Registrasi berhasil. Email verifikasi telah dikirim ke $email. Silakan verifikasi email Anda sebelum login.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            auth.signOut()
                                            startActivity(Intent(this, LoginActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                this,
                                                "Gagal menyimpan data pengguna: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(this, "Gagal mendapatkan ID pengguna", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Gagal mengirim email verifikasi: ${emailTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}