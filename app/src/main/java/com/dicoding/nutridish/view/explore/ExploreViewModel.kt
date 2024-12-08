package com.dicoding.nutridish.view.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.nutridish.data.api.response.ResponseItem
import com.dicoding.nutridish.data.api.retrofit.ApiConfig
import com.dicoding.nutridish.data.api.retrofit.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreViewModel : ViewModel() {

    private val apiService = ApiService

    private val _recipes = MutableLiveData<List<ResponseItem>>()
    val recipes: LiveData<List<ResponseItem>> get() = _recipes

    fun fetchRecipes(query: String) {
        ApiConfig.ApiService.searchRecipes(query).enqueue(object : Callback<List<ResponseItem>> {
            override fun onResponse(call: Call<List<ResponseItem>>, response: Response<List<ResponseItem>>) {
                if (response.isSuccessful) {
                    _recipes.value = response.body()
                }
            }

            override fun onFailure(call: Call<List<ResponseItem>>, t: Throwable) {
                // Handle error
                _recipes.value = emptyList()
            }
        })
    }
}