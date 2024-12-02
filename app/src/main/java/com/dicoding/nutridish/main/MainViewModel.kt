package com.dicoding.nutridish.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.api.response.LoginResponse
import com.dicoding.nutridish.data.api.response.RegisterResponse
import com.dicoding.nutridish.data.pref.UserModel
import com.dicoding.nutridish.data.pref.UserPreference
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository, private val userPreference: UserPreference) : ViewModel() {
    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse: LiveData<RegisterResponse> = _registerResponse

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.registerUser(name, email, password)
                _registerResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("RegisterError", e.message.toString())
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.loginUser(email, password)
                _loginResponse.postValue(response)

                if (!response.isError) {
                    response.loginResult?.let { result ->
                        saveSession(UserModel(result.userId, result.token, true))
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginError", e.message.toString())
            }
        }
    }

    fun getSession(): LiveData<UserModel> {
        return userPreference.getSession().asLiveData() // Convert Flow to LiveData
    }

    private fun saveSession(user: UserModel) {
        viewModelScope.launch {
            userPreference.saveSession(user)
        }
    }

    suspend fun logout() {
        userPreference.logout()
    }

}