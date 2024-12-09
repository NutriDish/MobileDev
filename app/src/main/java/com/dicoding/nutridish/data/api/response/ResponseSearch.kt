package com.dicoding.nutridish.data.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ApiResponse(
	@field:SerializedName("Response")
	val response: List<ResponseItem?>? = null
)

@Parcelize
data class MealType(

	@field:SerializedName("lunch")
	val lunch: Int? = null,

	@field:SerializedName("dessert")
	val dessert: Int? = null,

	@field:SerializedName("snack")
	val snack: Int? = null,

	@field:SerializedName("breakfast")
	val breakfast: Int? = null,

	@field:SerializedName("dinner")
	val dinner: Int? = null
) : Parcelable

@Parcelize
data class ResponseItem(

	@field:SerializedName("dietary")
	val dietary: Dietary? = null,

	@field:SerializedName("meal_type")
	val mealType: MealType? = null,

	@field:SerializedName("ingredients")
	val ingredients: Ingredients? = null,

	@field:SerializedName("title")
	val title: String? = null,

) : Parcelable

@Parcelize
data class Dietary(

	@field:SerializedName("vegetarian")
	val vegetarian: Int? = null,

	@field:SerializedName("vegan")
	val vegan: Int? = null,

	@field:SerializedName("soy free")
	val soyFree: Int? = null,

	@field:SerializedName("peanut free")
	val peanutFree: Int? = null,

	@field:SerializedName("wheat free")
	val wheatFree: Int? = null,

	@field:SerializedName("pescatarian")
	val pescatarian: Int? = null,

	@field:SerializedName("dairy free")
	val dairyFree: Int? = null,

	@field:SerializedName("paleo")
	val paleo: Int? = null,

	@field:SerializedName("low cal")
	val lowCal: Int? = null,

	@field:SerializedName("low cholesterol")
	val lowCholesterol: Int? = null,

	@field:SerializedName("low fat")
	val lowFat: Int? = null,

	@field:SerializedName("low carb")
	val lowCarb: Int? = null,

	@field:SerializedName("low sodium")
	val lowSodium: Int? = null,

	@field:SerializedName("fat free")
	val fatFree: Int? = null
) : Parcelable

@Parcelize
data class Ingredients(

	@field:SerializedName("carrot")
	val carrot: Int? = null,

	@field:SerializedName("tomato")
	val tomato: Int? = null,

	@field:SerializedName("pork")
	val pork: Int? = null,

	@field:SerializedName("egg")
	val egg: Int? = null,

	@field:SerializedName("chicken")
	val chicken: Int? = null,

	@field:SerializedName("cheese")
	val cheese: Int? = null,

	@field:SerializedName("onion")
	val onion: Int? = null,

	@field:SerializedName("eggplant")
	val eggplant: Int? = null,

	@field:SerializedName("fish")
	val fish: Int? = null,

	@field:SerializedName("pasta")
	val pasta: Int? = null,

	@field:SerializedName("beef")
	val beef: Int? = null,

	@field:SerializedName("rice")
	val rice: Int? = null,

	@field:SerializedName("peanut")
	val peanut: Int? = null,

	@field:SerializedName("potato")
	val potato: Int? = null,

	@field:SerializedName("shrimp")
	val shrimp: Int? = null,

	@field:SerializedName("cabbage")
	val cabbage: Int? = null,

	@field:SerializedName("tofu")
	val tofu: Int? = null,

	@field:SerializedName("bread")
	val bread: Int? = null,

	@field:SerializedName("zucchini")
	val zucchini: Int? = null
) : Parcelable
