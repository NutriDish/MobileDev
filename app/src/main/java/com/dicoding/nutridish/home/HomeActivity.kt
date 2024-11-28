package com.dicoding.nutridish.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.api.retrofit.ApiConfig
import com.dicoding.nutridish.data.pref.UserPreference
import com.dicoding.nutridish.data.pref.dataStore
import com.dicoding.nutridish.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch
class HomeActivity : AppCompatActivity() {
    private lateinit var repository: UserRepository
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ApiService and UserPreference
        val apiService = ApiConfig.getApiService()
        val userPreference = UserPreference.getInstance(dataStore) // Assuming dataStore is set up

        // Use the factory method to initialize the repository
        repository = UserRepository.getInstance(apiService, userPreference)

        binding.button.setOnClickListener {
            // Call logout in a coroutine
            lifecycleScope.launch {
                repository.logout()
                // Optionally, navigate or update UI after logout
            }
        }
    }
}