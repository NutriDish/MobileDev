package com.dicoding.nutridish.view.detail

import androidx.lifecycle.*
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.database.entity.NutriEntity
import com.dicoding.nutridish.data.api.response.ResponseRecipeDetail
import com.dicoding.nutridish.data.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(private val repository: UserRepository) : ViewModel() {

    private val _nutriDetail = MutableLiveData<Result<ResponseRecipeDetail>>()
    val nutriDetail: LiveData<Result<ResponseRecipeDetail>> get() = _nutriDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getNutriDetail(title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getNutriDetail(title)
                if (result.isSuccessful) {
                    result.body()?.let {
                        _nutriDetail.value = Result.Success(it)
                    } ?: run {
                        _nutriDetail.value = Result.Error("Response body is null")
                    }
                } else {
                    _nutriDetail.value = Result.Error("Failed: ${result.message()}")
                }
            } catch (e: Exception) {
                _nutriDetail.value = Result.Error("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setBookmark(recipe: NutriEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { // Run the database operation on IO dispatcher
                repository.setBookmark(recipe)
            }
        }
    }

    fun deleteBookmark(title: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { // Run the database operation on IO dispatcher
                repository.deleteBookmark(title)
            }
        }
    }

    fun checkBookmark(title: String): LiveData<NutriEntity?> {
        return repository.checkBookmark(title)

    }
}
