package com.dicoding.nutridish.data.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class ResponseRecipeDetail(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("sodium")
	val sodium: @RawValue Any? = null,

	@field:SerializedName("directions")
	val directions: List<String?>? = null,

	@field:SerializedName("protein")
	val protein: @RawValue Any? = null,

	@field:SerializedName("fat")
	val fat: @RawValue Any? = null,

	@field:SerializedName("rating")
	val rating: @RawValue Any? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("ingredients")
	val ingredients: List<String?>? = null,

	@field:SerializedName("calories")
	val calories: @RawValue Any? = null,

	@field:SerializedName("categories")
	val categories: List<String?>? = null,

	@field:SerializedName("title")
	val title: String? = null
) : Parcelable
