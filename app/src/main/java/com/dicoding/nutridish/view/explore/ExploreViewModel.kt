package com.dicoding.nutridish.view.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.api.response.ResponseItem
import com.dicoding.nutridish.data.api.retrofit.ApiConfig
import com.dicoding.nutridish.data.api.retrofit.ApiService
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreViewModel(private val repository: UserRepository) : ViewModel() {

    private val _recipes = MutableLiveData<List<ResponseItem?>?>()
    val recipes: LiveData<List<ResponseItem?>?> get() = _recipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun searchRecipes(query: String) {
        setLoading(true)
        viewModelScope.launch {
            val result = repository.searchRecipes(query)
            _recipes.postValue(result)
            setLoading(false)
        }
    }


    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

}