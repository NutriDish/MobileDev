package com.dicoding.nutridish.view.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SearchView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private val viewModel: ExploreViewModel by viewModels{
        ViewModelFactory.getInstance(requireActivity())
    }
    private lateinit var recipeAdapter: ExploreAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        // Find filter button
        val filterButton: View = view.findViewById(R.id.filter)
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewSearch)
        recipeAdapter = ExploreAdapter()
        recyclerView.apply {
            layoutManager = GridLayoutManager(context,2)
            adapter = recipeAdapter
        }

        searchRecipes("all")


        val searchView: androidx.appcompat.widget.SearchView = view.findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchRecipes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // Observe recipes LiveData
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes != null) {
                recipeAdapter.setRecipes(recipes.filterNotNull())
            }
        }
    }

    private fun searchRecipes(query: String) {
        lifecycleScope.launch {
            viewModel.searchRecipes(query)
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

            // Pass filters to filtering function
            filterRecipes(
                calories, protein, isBreakfast, isLunch
            )

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
}
