package com.dicoding.nutridish.personalization

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.nutridish.R

class AvoidAlcoholFragment : Fragment(R.layout.fragment_avoid_alcohol) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val finishButton = view.findViewById<Button>(R.id.btn_next)
        val avoidAlcoholCheckbox = view.findViewById<CheckBox>(R.id.cb_avoid_alcohol)
        val backButton = view.findViewById<Button>(R.id.btn_back)

        val sharedPreferences = requireActivity().getSharedPreferences("Personalization", AppCompatActivity.MODE_PRIVATE)
        val savedAvoidAlcohol = sharedPreferences.getBoolean("avoidAlcohol", false)
        avoidAlcoholCheckbox.isChecked = savedAvoidAlcohol

        finishButton.setOnClickListener {
            val avoidAlcohol = avoidAlcoholCheckbox.isChecked
            sharedPreferences.edit().putBoolean("avoidAlcohol", avoidAlcohol).apply()
            (activity as PersonalizeActivity).completePersonalization()
        }

        backButton.setOnClickListener {
            (activity as PersonalizeActivity).saveAndBack(AvoidPorkFragment())
        }
    }
}
