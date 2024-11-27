@file:Suppress("UNCHECKED_CAST")

package com.dicoding.nutridish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.pref.UserPreference
import com.dicoding.nutridish.data.pref.dataStore
import com.dicoding.nutridish.di.Injection
import com.dicoding.nutridish.main.MainViewModel

class ViewModelFactory private constructor(
    private val userRepository: UserRepository,
    private val userPreference: UserPreference
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userRepository, userPreference) as T
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