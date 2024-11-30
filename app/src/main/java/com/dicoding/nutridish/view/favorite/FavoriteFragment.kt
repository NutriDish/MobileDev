package com.dicoding.nutridish.view.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.nutridish.databinding.FragmentFavoriteBinding
import com.dicoding.nutridish.data.api.response.ListEventsItem

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding
    private lateinit var favoriteAdapter: EventAdapter
    private var dataList: List<ListEventsItem> = emptyList() // Simulasikan data di sini

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFavoriteBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteAdapter = EventAdapter()

        // Setup RecyclerView
        binding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = favoriteAdapter
        }

        // Check if dataList is empty
        updateUI()

        // Setup SearchView
        binding?.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (!newText.isNullOrEmpty()) {
                    dataList.filter { it.name.contains(newText, ignoreCase = true) }
                } else {
                    dataList
                }
                favoriteAdapter.submitList(filteredList)
                updateUI(filteredList.isEmpty())
                return true
            }
        })
    }

    private fun updateUI(isEmpty: Boolean = dataList.isEmpty()) {
        if (isEmpty) {
            binding?.recyclerView?.visibility = View.GONE
            binding?.tvEmpty?.visibility = View.VISIBLE // Pastikan TextView tvEmpty sudah ada di layout
        } else {
            binding?.recyclerView?.visibility = View.VISIBLE
            binding?.tvEmpty?.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
