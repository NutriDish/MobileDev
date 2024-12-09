package com.dicoding.nutridish.view.explore

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.nutridish.R
import com.dicoding.nutridish.data.api.response.ResponseItem
import com.dicoding.nutridish.databinding.ItemRecentlyAddedBinding
import com.dicoding.nutridish.view.detail.DetailActivity

class ExploreAdapter(
    private val onLoading: (Boolean) -> Unit
) : ListAdapter<ResponseItem, ExploreAdapter.RecipeViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ResponseItem>() {
            override fun areItemsTheSame(oldItem: ResponseItem, newItem: ResponseItem): Boolean {
                return oldItem.title == newItem.title // Sesuaikan dengan ID unik
            }

            override fun areContentsTheSame(oldItem: ResponseItem, newItem: ResponseItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun updateRecipes(newRecipes: List<ResponseItem>) {
        submitList(newRecipes)
    }

    class RecipeViewHolder(private val binding: ItemRecentlyAddedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: ResponseItem, onLoading: (Boolean) -> Unit) {
            onLoading(true)
            binding.textFoodName.text = recipe.title
//            Glide.with(binding.imageFood.context)
//                .load(recipe.imageUrl)
//                .placeholder(R.drawable.placeholder_image) // Gambar placeholder
//                .error(R.drawable.error_image) // Gambar error
//                .into(binding.imageFood)
            binding.cardView.setOnClickListener {

                val context = itemView.context
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("recipe_data_list", recipe)
                context.startActivity(intent)

            }
            onLoading(false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecentlyAddedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position), onLoading)
    }
}
