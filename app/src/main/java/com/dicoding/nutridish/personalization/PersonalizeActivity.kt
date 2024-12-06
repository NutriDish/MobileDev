package com.dicoding.nutridish.personalization

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dicoding.nutridish.main.MainActivity
import com.dicoding.nutridish.R
import com.dicoding.nutridish.databinding.ActivityPersonalizeBinding
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

        val sharedPreferences = getSharedPreferences("Personalization", MODE_PRIVATE)
        val isFirstLogin = sharedPreferences.getBoolean("isFirstLogin", true)

        if (!isFirstLogin) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_personalize)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AgeFragment())
            .commit()
    }

    fun saveAndNext(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun completePersonalization() {
        val sharedPreferences = getSharedPreferences("Personalization", MODE_PRIVATE)
        val age = sharedPreferences.getInt("age", 0)
        val weight = sharedPreferences.getInt("weight", 0)
        val avoidPork = sharedPreferences.getBoolean("avoidPork", false)
        val avoidAlcohol = sharedPreferences.getBoolean("avoidAlcohol", false)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val data = mapOf(
                "age" to age,
                "weight" to weight,
                "tags.pork" to avoidPork,
                "tags.alcohol" to avoidAlcohol
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