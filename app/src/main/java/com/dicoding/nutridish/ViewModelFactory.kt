@file:Suppress("UNCHECKED_CAST")

package com.dicoding.nutridish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.pref.UserPreference
import com.dicoding.nutridish.data.pref.dataStore
import com.dicoding.nutridish.di.Injection
import com.dicoding.nutridish.login.LoginViewModel
import com.dicoding.nutridish.main.MainViewModel
import com.dicoding.nutridish.view.dashboard.DashboardViewModel
import com.dicoding.nutridish.view.detail.DetailViewModel
import com.dicoding.nutridish.view.explore.ExploreViewModel
import com.dicoding.nutridish.view.explore.upload.UploadImageViewModel
import com.dicoding.nutridish.view.favorite.FavoriteViewModel
import com.dicoding.nutridish.view.profile.ProfileViewModel

class ViewModelFactory private constructor(
    private val userRepository: UserRepository,
    private val userPreference: UserPreference
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userRepository, userPreference) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userPreference) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userPreference) as T
            }
            modelClass.isAssignableFrom(ExploreViewModel::class.java) -> {
                ExploreViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> {
                FavoriteViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(UploadImageViewModel::class.java) -> {
                UploadImageViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            val repository = Injection.provideRepository(context)
            val userPreference = UserPreference.getInstance(context.dataStore)
            return INSTANCE ?: synchronized(this) {
                val instance = ViewModelFactory(repository, userPreference)
                INSTANCE = instance
                instance
            }
        }
    }
}