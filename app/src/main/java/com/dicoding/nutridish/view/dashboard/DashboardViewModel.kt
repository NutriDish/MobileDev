package com.dicoding.nutridish.view.dashboard

import android.nfc.tech.MifareUltralight.PAGE_SIZE
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.api.response.ResponseItem
import com.dicoding.nutridish.data.database.entity.NutriEntity
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: UserRepository
) : ViewModel(){
    private val _recipesRecommended = MutableLiveData<List<ResponseItem?>?>(mutableListOf())
    val recipesRecommended: LiveData<List<ResponseItem?>?> get() = _recipesRecommended

    private val _recipesToday = MutableLiveData<List<ResponseItem?>?>(mutableListOf())
    val recipesToday: LiveData<List<ResponseItem?>?> get() = _recipesToday

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var currentPage = 1
    private var isAllDataLoaded = false
    private var lastQuery: String? = null
    private var lastFilters: String? = null
    val getFavoriteRecipe: LiveData<List<NutriEntity>> = repository.getBookmarkedNutri()

    fun getRecommendedRecipe(query: String, filters: String? = null) {
        // Reset pagination if query or filters change
        if (query != lastQuery || filters != lastFilters) {
            currentPage = 1
            isAllDataLoaded = false
            _recipesRecommended.value = mutableListOf()
            lastQuery = query
            lastFilters = filters
        }

        if (isAllDataLoaded) return

        setLoading(true)
        viewModelScope.launch {
            try {
                val result = repository.loadRecipes(query, filters, currentPage)

                if (result.isNullOrEmpty()) {
                    isAllDataLoaded = true
                } else {
                    val currentList = _recipesRecommended.value?.toMutableList() ?: mutableListOf()
                    currentList.addAll(result)
                    _recipesRecommended.postValue(currentList)
                    currentPage++
                }
            } catch (e: Exception) {
                _recipesRecommended.postValue(emptyList())
            } finally {
                setLoading(false)
            }
        }
    }

    fun getTodayRecipe(query: String, filters: String? = null) {
        // Reset pagination jika query atau filters berubah
        if (query != lastQuery || filters != lastFilters) {
            currentPage = 1
            isAllDataLoaded = false
            _recipesToday.value = mutableListOf()
            lastQuery = query
            lastFilters = filters
        }

        if (isAllDataLoaded) return

        setLoading(true)
        viewModelScope.launch {
            try {
                val result = repository.loadRecipesToday(query, filters, currentPage)

                if (result.isNullOrEmpty()) {
                    isAllDataLoaded = true
                } else {
                    val currentList = _recipesToday.value?.toMutableList() ?: mutableListOf()
                    currentList.addAll(result)
                    _recipesToday.postValue(currentList)
                    currentPage++
                }
            } catch (e: Exception) {
                _recipesToday.postValue(emptyList())
            } finally {
                setLoading(false)
            }
        }
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

}