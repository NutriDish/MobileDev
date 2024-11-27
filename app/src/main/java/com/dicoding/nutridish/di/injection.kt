package com.dicoding.nutridish.di

import android.content.Context
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.api.ApiConfig
import com.dicoding.nutridish.data.pref.UserPreference
import com.dicoding.nutridish.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(apiService, pref)
    }
}