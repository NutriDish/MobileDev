package com.dicoding.nutridish.view.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.nutridish.data.UserRepository
import com.dicoding.nutridish.data.database.entity.NutriEntity

class DashboardViewModel(
    private val repository: UserRepository
) : ViewModel(){

    val getFavoriteRecipe: LiveData<List<NutriEntity>> = repository.getBookmarkedNutri()


}