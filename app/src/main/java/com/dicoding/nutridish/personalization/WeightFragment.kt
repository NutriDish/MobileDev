package com.dicoding.nutridish.personalization

import android.annotation.SuppressLint
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

class WeightFragment : Fragment(R.layout.fragment_weight) {
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = view.findViewById<Button>(R.id.btn_next)
        val weightInput = view.findViewById<EditText>(R.id.et_weight)

        val sharedPreferences = requireActivity().getSharedPreferences("Personalization", AppCompatActivity.MODE_PRIVATE)
        val savedWeight = sharedPreferences.getInt("weight", 0)
        if (savedWeight > 0) {
            weightInput.setText(savedWeight.toString())
        }

        nextButton.setOnClickListener {
            val weight = weightInput.text.toString().toIntOrNull()
            if (weight != null) {
                sharedPreferences.edit().putInt("weight", weight).apply()
                (activity as PersonalizeActivity).saveAndNext(AvoidPorkFragment())
            } else {
                Toast.makeText(context, "Masukkan berat badan yang valid", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
