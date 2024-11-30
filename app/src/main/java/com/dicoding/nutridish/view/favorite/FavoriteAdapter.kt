package com.dicoding.nutridish.view.favorite

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.nutridish.data.api.response.ListEventsItem
import com.dicoding.nutridish.databinding.ItemFavoriteBinding
import com.dicoding.nutridish.view.detail.DetailActivity

class EventAdapter(

) : ListAdapter<ListEventsItem, EventAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListEventsItem>() {
            override fun areItemsTheSame(oldItem: ListEventsItem, newItem: ListEventsItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListEventsItem, newItem: ListEventsItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    class MyViewHolder(private val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(event: ListEventsItem) {
            binding.tvItemName.text = event.name
            Glide.with(binding.imgItemPhoto.context)
                .load(event.mediaCover)
                .into(binding.imgItemPhoto)

            val itemDataList = "event_data_list"
            binding.cardView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra(itemDataList, event)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}