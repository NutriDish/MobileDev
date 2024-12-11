package com.dicoding.nutridish.view.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.api.response.ResponseItem
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: UserRepository) : ViewModel() {

    private val _recipes = MutableLiveData<List<ResponseItem?>?>(mutableListOf())
    val recipes: LiveData<List<ResponseItem?>?> get() = _recipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var currentPage = 1
    private var isAllDataLoaded = false
    private var lastQuery: String? = null
    private var lastFilters: String? = null

    fun searchRecipes(query: String, filters: String? = null) {
        // Reset pagination if query or filters change
        if (query != lastQuery || filters != lastFilters) {
            currentPage = 1
            isAllDataLoaded = false
            _recipes.value = mutableListOf()
            lastQuery = query
            lastFilters = filters
        }

        if (isAllDataLoaded) return

        setLoading(true)
        viewModelScope.launch {
            try {
                val result = repository.searchRecipes(query, filters, currentPage)

                if (result.isNullOrEmpty()) {
                    isAllDataLoaded = true
                } else {
                    val currentList = _recipes.value?.toMutableList() ?: mutableListOf()
                    currentList.addAll(result)
                    _recipes.postValue(currentList)
                    currentPage++
                }
            } catch (e: Exception) {
                _recipes.postValue(emptyList())
            } finally {
                setLoading(false)
            }
        }
    }

    fun loadMoreRecipes() {
        if (lastQuery != null && !isAllDataLoaded && !(_isLoading.value == true)) {
            searchRecipes(lastQuery!!, lastFilters)
        }
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
}