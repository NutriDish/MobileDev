package com.dicoding.nutridish.view.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.database.entity.NutriEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteViewModel(private val repository: UserRepository) : ViewModel() {
    val favoriteNutri: LiveData<List<NutriEntity>> = repository.getBookmarkedNutri()
}