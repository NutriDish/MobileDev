package com.dicoding.nutridish.personalization

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.nutridish.main.MainActivity
import com.dicoding.nutridish.R
import com.dicoding.nutridish.databinding.ActivityPersonalizeBinding
import com.dicoding.nutridish.view.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalizeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalizeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.saveButton.setOnClickListener {
            val weight = binding.weightEditText.text.toString().toIntOrNull()
            val age = binding.ageEditText.text.toString().toIntOrNull()
            val avoidPork = binding.avoidPorkCheckbox.isChecked
            val avoidAlcohol = binding.avoidAlcoholCheckbox.isChecked

            if (weight != null && age != null) {
                savePersonalizationData(weight, age, avoidPork, avoidAlcohol)
            } else {
                Toast.makeText(this, "Masukkan data yang valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePersonalizationData(weight: Int, age: Int, avoidPork: Boolean, avoidAlcohol: Boolean) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val updates = mapOf(
                "weight" to weight,
                "age" to age,
                "tags.pork" to avoidPork,
                "tags.alcohol" to avoidAlcohol
            )

            firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data personalisasi berhasil disimpan", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}
