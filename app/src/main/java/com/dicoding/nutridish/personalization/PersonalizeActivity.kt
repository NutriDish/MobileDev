package com.dicoding.nutridish.personalization

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dicoding.nutridish.R
import com.dicoding.nutridish.view.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalizeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        auth.currentUser?.let { user ->
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val weight = document.getLong("weight")?.toInt()
                    if (weight != null && weight > 0) {
                        // Jika weight ada dan lebih besar dari 0, arahkan ke HomeActivity
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        // Jika weight null atau 0, tetap di PersonalizeActivity
                        setContentView(R.layout.activity_personalize)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, WeightFragment())
                            .commit()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memeriksa data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        setContentView(R.layout.activity_personalize)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, WeightFragment())
            .commit()
    }

    fun saveAndNext(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun saveAndBack(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


    fun completePersonalization() {
        val sharedPreferences = getSharedPreferences("Personalization", MODE_PRIVATE)
        val weight = sharedPreferences.getInt("weight", 0)
        val avoidPork = sharedPreferences.getBoolean("avoidPork", false)
        val avoidAlcohol = sharedPreferences.getBoolean("avoidAlcohol", false)

        val porkValue = if (avoidPork) 1 else 0
        val alcoholValue = if (avoidAlcohol) 1 else 0

        val userId = auth.currentUser?.uid
        if (userId != null) {

            val data = mapOf(
                "weight" to weight,
                "cons_pork" to porkValue,
                "cons_alcohol" to alcoholValue,
            )

            firestore.collection("users").document(userId)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    sharedPreferences.edit().putBoolean("isFirstLogin", false).apply()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}