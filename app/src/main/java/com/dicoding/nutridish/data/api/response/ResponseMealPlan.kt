package com.dicoding.nutridish.data.api.response

import com.google.gson.annotations.SerializedName

data class ResponseMealPlan(

	@field:SerializedName("meal_plan")
	val mealPlan: MealPlan? = null,

	@field:SerializedName("user_id")
	val userId: String? = null
)

data class DessertItem(

	@field:SerializedName("date_used")
	val dateUsed: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)

data class MealPlan(

	@field:SerializedName("lunch")
	val lunch: List<LunchItem?>? = null,

	@field:SerializedName("dessert")
	val dessert: List<DessertItem?>? = null,

	@field:SerializedName("snack")
	val snack: List<SnackItem?>? = null,

	@field:SerializedName("breakfast")
	val breakfast: List<BreakfastItem?>? = null,

	@field:SerializedName("dinner")
	val dinner: List<DinnerItem?>? = null
)

data class SnackItem(

	@field:SerializedName("date_used")
	val dateUsed: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)

data class BreakfastItem(

	@field:SerializedName("date_used")
	val dateUsed: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)

data class DinnerItem(

	@field:SerializedName("date_used")
	val dateUsed: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)

data class LunchItem(

	@field:SerializedName("date_used")
	val dateUsed: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)
