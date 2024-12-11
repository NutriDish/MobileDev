package com.dicoding.nutridish.personalization

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.nutridish.R

class AvoidPorkFragment : Fragment(R.layout.fragment_avoid_pork) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = view.findViewById<Button>(R.id.btn_next)
        val avoidPorkCheckbox = view.findViewById<CheckBox>(R.id.cb_avoid_pork)
        val backButton = view.findViewById<Button>(R.id.btn_back)

        val sharedPreferences = requireActivity().getSharedPreferences("Personalization", AppCompatActivity.MODE_PRIVATE)
        val savedAvoidPork = sharedPreferences.getBoolean("avoidPork", false)
        avoidPorkCheckbox.isChecked = savedAvoidPork

        nextButton.setOnClickListener {
            val avoidPork = avoidPorkCheckbox.isChecked
            sharedPreferences.edit().putBoolean("avoidPork", avoidPork).apply()
            (activity as PersonalizeActivity).saveAndNext(AvoidAlcoholFragment())
        }

        backButton.setOnClickListener {
            (activity as PersonalizeActivity).saveAndBack(WeightFragment())
        }
    }
}
