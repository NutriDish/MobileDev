package com.dicoding.nutridish.view.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.FragmentFavoriteBinding
import com.dicoding.nutridish.data.api.response.ResponseItem

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding
    private lateinit var favoriteAdapter: FavoriteAdapter
    private var dataList: List<ResponseItem> = emptyList()

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



        val factory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: FavoriteViewModel by viewModels {
            factory
        }
        // Setup Adapter
        favoriteAdapter = FavoriteAdapter()

        // Mengamati LiveData dari ViewModel
        viewModel.favoriteNutri.observe(viewLifecycleOwner) { favoriteList ->
            if (favoriteList != null){
                val items = arrayListOf<ResponseItem>()
                favoriteList.map {
                    val item = ResponseItem(
                        title = it.title,
                    )
                    items.add(item)
                }
                favoriteAdapter.submitList(items)

            }

        }


        // Setup RecyclerView
        binding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = favoriteAdapter
        }


        // Setup SearchView
        binding?.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (!newText.isNullOrEmpty()) {
                    dataList.filter { it.title?.contains(newText, ignoreCase = true) ?: false  }
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
            binding?.tvEmpty?.visibility = View.VISIBLE
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
