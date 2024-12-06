package com.dicoding.nutridish.login

import androidx.lifecycle.ViewModel
import com.dicoding.nutridish.data.pref.UserModel
import com.dicoding.nutridish.data.pref.UserPreference

class LoginViewModel(private val userPreference: UserPreference): ViewModel() {
    suspend fun saveSession(email :String, password: String, isLogin :Boolean) {
        userPreference.saveSession(UserModel(email, password, isLogin))
    }
}