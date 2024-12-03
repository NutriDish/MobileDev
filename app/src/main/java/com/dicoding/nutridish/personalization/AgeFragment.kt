package com.dicoding.nutridish.personalization

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.nutridish.R

class AgeFragment : Fragment(R.layout.fragment_age) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = view.findViewById<Button>(R.id.btn_next)
        val ageInput = view.findViewById<EditText>(R.id.et_age)

        nextButton.setOnClickListener {
            val age = ageInput.text.toString().toIntOrNull()
            if (age != null) {
                val sharedPreferences = requireActivity().getSharedPreferences("Personalization", AppCompatActivity.MODE_PRIVATE)
                sharedPreferences.edit().putInt("age", age).apply()

                (activity as PersonalizeActivity).saveAndNext(WeightFragment())
            } else {
                Toast.makeText(context, "Masukkan umur yang valid", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
