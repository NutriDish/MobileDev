package com.dicoding.nutridish.view.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.nutridish.data.api.response.ResponseItem
import com.dicoding.nutridish.databinding.ItemFavoriteBinding
import com.dicoding.nutridish.databinding.ItemRecentlyAddedBinding
import com.dicoding.nutridish.view.detail.DetailActivity

class DashboardAdapter() : ListAdapter<ResponseItem, DashboardAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ResponseItem>() {
            override fun areItemsTheSame(oldItem: ResponseItem, newItem: ResponseItem): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: ResponseItem, newItem: ResponseItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    class MyViewHolder(private val binding: ItemRecentlyAddedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(event: ResponseItem) {
            Log.d("RecyclerView", "Binding item: ${event.title}")
            binding.textFoodName.text = event.title
//            Glide.with(binding.imgItemPhoto.context)
//                .load(event.mediaCover)
//                .into(binding.imgItemPhoto)
            val itemDataList = "recipe_data_list"
            binding.cardView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra(itemDataList, event)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemRecentlyAddedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}