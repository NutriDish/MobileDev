package com.dicoding.nutridish.data.api.response

import com.google.gson.annotations.SerializedName

data class ResponseRecipeDetail(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("sodium")
	val sodium: Any? = null,

	@field:SerializedName("directions")
	val directions: List<String?>? = null,

	@field:SerializedName("protein")
	val protein: Any? = null,

	@field:SerializedName("fat")
	val fat: Any? = null,

	@field:SerializedName("rating")
	val rating: Any? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("ingredients")
	val ingredients: List<String?>? = null,

	@field:SerializedName("calories")
	val calories: Any? = null,

	@field:SerializedName("categories")
	val categories: List<String?>? = null,

	@field:SerializedName("title")
	val title: String? = null
)
