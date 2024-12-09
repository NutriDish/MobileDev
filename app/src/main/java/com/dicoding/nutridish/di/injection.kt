package com.dicoding.nutridish.di

import android.content.Context
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.api.retrofit.ApiConfig
import com.dicoding.nutridish.data.database.room.NutriDatabase
import com.dicoding.nutridish.data.pref.UserPreference
import com.dicoding.nutridish.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        val database = NutriDatabase.getInstance(context)
        val dao = database.nutriDao()
        return UserRepository.getInstance(apiService, pref, dao)
    }
}