package com.dicoding.nutridish.view.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.FragmentExploreBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private val viewModel: ExploreViewModel by viewModels{
        ViewModelFactory.getInstance(requireActivity())
    }
    private lateinit var recipeAdapter: ExploreAdapter
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filter.setOnClickListener {
            showFilterDialog()
        }

        // Initialize RecyclerView and Adapter
        recipeAdapter = ExploreAdapter { isLoading ->
            viewModel.setLoading(isLoading)
        }

        binding.recyclerViewSearch.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = recipeAdapter
        }


        // Setup SearchView
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchRecipes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Observe recipes LiveData
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes != null) {
                recipeAdapter.updateRecipes(recipes.filterNotNull())
            }
        }
        // Search for all recipes
        searchRecipes("all")

    }

    private fun searchRecipes(query: String) {
        viewModel.setLoading(true) // Set loading true saat pencarian dimulai
        lifecycleScope.launch {
            viewModel.searchRecipes(query)
            viewModel.setLoading(false) // Set loading false setelah pencarian selesai
        }
    }

    private fun showFilterDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.filter_dialog, null)
        bottomSheetDialog.setContentView(dialogView)

        // Float filters
        val caloriesInput = dialogView.findViewById<EditText>(R.id.caloriesInput)
        val proteinInput = dialogView.findViewById<EditText>(R.id.proteinInput)

        // Boolean filters
        val filterBreakfast = dialogView.findViewById<CheckBox>(R.id.filterBreakfast)
        val filterLunch = dialogView.findViewById<CheckBox>(R.id.filterLunch)

        // Apply button
        val applyFiltersButton = dialogView.findViewById<Button>(R.id.applyFiltersButton)
        applyFiltersButton.setOnClickListener {
            // Collect float filters
            val calories = caloriesInput.text.toString().toFloatOrNull() ?: 0f
            val protein = proteinInput.text.toString().toFloatOrNull() ?: 0f

            // Collect boolean filters
            val isBreakfast = filterBreakfast.isChecked
            val isLunch = filterLunch.isChecked

            // Set loading true before filtering
            viewModel.setLoading(true)

            // Pass filters to filtering function
            filterRecipes(
                calories, protein, isBreakfast, isLunch
            )

            // After filtering is done
            viewModel.setLoading(false)

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun filterRecipes(
        calories: Float, protein: Float, isBreakfast: Boolean,
        isLunch: Boolean
    ) {
        // Implement logic to filter recipes based on the parameters
        // Example: send the filters to ViewModel or directly query the data source
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
