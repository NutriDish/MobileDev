package com.dicoding.nutridish.view.profile

import androidx.lifecycle.ViewModel
import com.dicoding.nutridish.data.pref.UserPreference

class ProfileViewModel(private val userPreference: UserPreference): ViewModel() {
    suspend fun logout() {
        userPreference.logout()
    }
}