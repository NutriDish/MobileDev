package com.dicoding.nutridish.view.profile.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.nutridish.databinding.ActivityUpdateProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.changeButton.setOnClickListener {
            val userName = binding.nameEditText.text.toString()
            val dateOfBirth = binding.dateEditTextLayout.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (validateInputs(password)) { // Hanya password yang divalidasi
                updateUserData(userName, dateOfBirth, password)
            }
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }

    }

    private fun validateInputs(password: String): Boolean {
        if (password.isBlank()) {
            Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun calculateAge(dob: String): Int {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val birthDate = dateFormat.parse(dob)
            val today = Calendar.getInstance()

            val birthCalendar = Calendar.getInstance().apply {
                time = birthDate!!
            }

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age -= 1
            }
            age
        } catch (e: Exception) {
            -1
        }
    }

    private fun formatDateOfBirth(dobInput: String): String? {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dobInput)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            null
        }
    }

    private fun updateUserData(name: String?, dobInput: String?, password: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        // Re-authenticate the user
        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userUpdates = mutableMapOf<String, Any>()

                // Tambahkan nama jika tidak kosong
                if (!name.isNullOrBlank()) {
                    userUpdates["userName"] = name
                }

                // Format dan tambahkan tanggal lahir jika tidak kosong
                if (!dobInput.isNullOrBlank()) {
                    val formattedDob = formatDateOfBirth(dobInput)
                    if (!formattedDob.isNullOrEmpty()) {
                        userUpdates["dateBirth"] = formattedDob

                        // Hitung usia hanya jika tanggal lahir valid
                        val age = calculateAge(dobInput)
                        if (age >= 0) {
                            userUpdates["age"] = age
                        } else {
                            Toast.makeText(this, "Tanggal lahir tidak valid", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                    } else {
                        Toast.makeText(this, "Format tanggal lahir tidak valid", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                }

                // Perbarui Firestore jika ada field yang ingin diubah
                if (userUpdates.isNotEmpty()) {
                    firestore.collection("users").document(user.uid)
                        .update(userUpdates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal memperbarui data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Tidak ada perubahan data", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Autentikasi gagal. Periksa password Anda", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
