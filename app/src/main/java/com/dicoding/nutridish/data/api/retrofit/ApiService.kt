package com.dicoding.nutridish.data.api.retrofit

import com.dicoding.nutridish.data.api.response.LoginResponse
import com.dicoding.nutridish.data.api.response.RegisterResponse
import com.dicoding.nutridish.data.api.response.Response
import com.dicoding.nutridish.data.api.response.ResponseItem
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.GET
import retrofit2.Call

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("recipes/search")
    suspend fun searchRecipes(
        @Query("query") query: String
    ): Call<List<ResponseItem>>
        // recipes/search?query=
}