package com.dicoding.nutridish.data.api.retrofit

import com.dicoding.nutridish.data.api.response.FileUploadResponse
import com.dicoding.nutridish.data.api.response.LoginResponse
import com.dicoding.nutridish.data.api.response.RegisterResponse
import com.dicoding.nutridish.data.api.response.ResponseItem
import com.dicoding.nutridish.data.api.response.ResponseRecipeDetail
import okhttp3.MultipartBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path


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
        @Query("query") query: String,
        @Query("filters") filters: String? = null
    ): Response<List<ResponseItem>>

    @GET("recipe_details/{title}/")
    suspend fun getRecipeDetail(
        @Path("title") title: String
    ): Response<ResponseRecipeDetail>

    @Multipart
    @POST("image/{imageFile}")
    suspend fun uploadImage(
        @Path("imageFile") imageFile: String, // Nama file yang dikirim ke server
        @Part file: MultipartBody.Part  // Bagian file gambar
    ): FileUploadResponse


}