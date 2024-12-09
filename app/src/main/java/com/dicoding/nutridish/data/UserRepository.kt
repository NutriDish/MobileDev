package com.dicoding.nutridish.data

import androidx.lifecycle.LiveData
import com.dicoding.nutridish.data.api.response.LoginResponse
import com.dicoding.nutridish.data.api.response.RegisterResponse
import com.dicoding.nutridish.data.api.response.ResponseItem
import com.dicoding.nutridish.data.api.response.ResponseRecipeDetail
import com.dicoding.nutridish.data.api.retrofit.ApiService
import com.dicoding.nutridish.data.database.entity.NutriEntity
import com.dicoding.nutridish.data.database.room.NutriDao
import com.dicoding.nutridish.data.pref.UserModel
import com.dicoding.nutridish.data.pref.UserPreference
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val nutriDao: NutriDao
) {
    suspend fun getNutriDetail(title: String): Response<ResponseRecipeDetail> {
        return apiService.getRecipeDetail(title)
    }

    fun getBookmarkedNutri(): LiveData<List<NutriEntity>> {
        return nutriDao.getBookmarkedNutri() // Mengambil data dari Room sebagai LiveData
    }

    suspend fun setBookmark(recipe: NutriEntity) {
        nutriDao.insertNutri(recipe)
    }

    suspend fun deleteBookmark(title: String) {
        nutriDao.deleteNutriById(title)
    }

    fun checkBookmark(id: String): LiveData<NutriEntity?> {
        return nutriDao.getFavoriteNutriById(id)
    }

    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun loginUser(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }
    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }
    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun searchRecipes(query: String): List<ResponseItem?>? {
        return try {
            val response = apiService.searchRecipes(query)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference, dao: NutriDao): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserRepository(userPreference, apiService, dao)
                INSTANCE = instance
                instance
            }
        }
    }
}
