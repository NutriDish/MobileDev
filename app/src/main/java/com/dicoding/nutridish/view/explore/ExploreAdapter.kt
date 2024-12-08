package com.dicoding.nutridish.view.explore

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.nutridish.R
import com.dicoding.nutridish.data.api.response.ResponseItem

class ExploreAdapter : RecyclerView.Adapter<ExploreAdapter.RecipeViewHolder>() {

    private val recipes = mutableListOf<ResponseItem>()

    // Update data in the adapter
    @SuppressLint("NotifyDataSetChanged")
    fun setRecipes(newRecipes: List<ResponseItem>) {
        recipes.clear()
        recipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recently_added, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val imageFood: ImageView = itemView.findViewById(R.id.imageFood)
        private val textFoodName: TextView = itemView.findViewById(R.id.textFoodName)


        fun bind(recipe: ResponseItem) {
            // Bind data to views
            textFoodName.text = recipe.title
        }
    }
}
