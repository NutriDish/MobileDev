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

        recipeAdapter = ExploreAdapter { isLoading ->
            viewModel.setLoading(isLoading)
        }

        binding.recyclerViewSearch.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = recipeAdapter
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchRecipes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        }
        )

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            if (recipes != null) {
                recipeAdapter.updateRecipes(recipes.filterNotNull())
            }
        }
        searchRecipes("")

    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.filter_dialog, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val breakfastCheckBox = dialogView.findViewById<CheckBox>(R.id.filterBreakfast)
        val lunchCheckBox = dialogView.findViewById<CheckBox>(R.id.filterLunch)
        val dinnerCheckBox = dialogView.findViewById<CheckBox>(R.id.filterdinner)

        val snackcheckbox = dialogView.findViewById<CheckBox>(R.id.filtersnack)
        val desertcheckbox = dialogView.findViewById<CheckBox>(R.id.filterdesert)
        val vegetariancheckbox = dialogView.findViewById<CheckBox>(R.id.filtervegetarian)
        val vegancheckbox = dialogView.findViewById<CheckBox>(R.id.filtervegan)
        val glutenfreecheckbox = dialogView.findViewById<CheckBox>(R.id.filterglutenfree)
        val dairyfreecheckbox = dialogView.findViewById<CheckBox>(R.id.filterdairyfree)
        val pescatariancheckbox = dialogView.findViewById<CheckBox>(R.id.filterpescatarian)
        val paleocheckbox = dialogView.findViewById<CheckBox>(R.id.filterpaleo)
        val peanutfreecheckbox = dialogView.findViewById<CheckBox>(R.id.filterpeanutfree)
        val soyfreecheckbox = dialogView.findViewById<CheckBox>(R.id.filtersoyfree)
        val lowcaloriecheckbox = dialogView.findViewById<CheckBox>(R.id.filterlowcal)
        val lowcholesterolcheckbox = dialogView.findViewById<CheckBox>(R.id.filterlowcholesterol)
        val lowfatcheckbox = dialogView.findViewById<CheckBox>(R.id.filterlowfat)
        val lowcarbcheckbox = dialogView.findViewById<CheckBox>(R.id.filterlowcarb)
        val lowsodiumcheckbox = dialogView.findViewById<CheckBox>(R.id.filterlowsodium)
        val fatfreecheckbox = dialogView.findViewById<CheckBox>(R.id.filterfatfree)
        val lowsugarcheckbox = dialogView.findViewById<CheckBox>(R.id.filterlowsugar)

        val applyButton = dialogView.findViewById<Button>(R.id.applyFiltersButton)

        applyButton.setOnClickListener {

            val selectedFilters = mutableListOf<String>()
            if (breakfastCheckBox.isChecked) selectedFilters.add("breakfast")
            if (lunchCheckBox.isChecked) selectedFilters.add("lunch")
            if (dinnerCheckBox.isChecked) selectedFilters.add("dinner")

            if (snackcheckbox.isChecked) selectedFilters.add("snack")
            if (desertcheckbox.isChecked) selectedFilters.add("desert")
            if (vegetariancheckbox.isChecked) selectedFilters.add("vegetarian")
            if (vegancheckbox.isChecked) selectedFilters.add("vegan")
            if (glutenfreecheckbox.isChecked) selectedFilters.add("gluten free")
            if (dairyfreecheckbox.isChecked) selectedFilters.add("dairy free")
            if (pescatariancheckbox.isChecked) selectedFilters.add("pescatarian")
            if (paleocheckbox.isChecked) selectedFilters.add("paleo")
            if (peanutfreecheckbox.isChecked) selectedFilters.add("peanut free")
            if (soyfreecheckbox.isChecked) selectedFilters.add("soy free")
            if (lowcaloriecheckbox.isChecked) selectedFilters.add("low cal")
            if (lowcholesterolcheckbox.isChecked) selectedFilters.add("low cholesterol")
            if (lowfatcheckbox.isChecked) selectedFilters.add("low fat")
            if (lowcarbcheckbox.isChecked) selectedFilters.add("low carb")
            if (lowsodiumcheckbox.isChecked) selectedFilters.add("low sodium")
            if (fatfreecheckbox.isChecked) selectedFilters.add("fat free")
            if (lowsugarcheckbox.isChecked) selectedFilters.add("low sugar")

            val filterQuery = selectedFilters.joinToString(",")
            dialog.dismiss()

            val query = binding.searchView.query.toString()

            searchRecipes(query, if (selectedFilters.isNotEmpty()) filterQuery else null)
        }

        dialog.show()
    }

    private fun searchRecipes(query: String, filters: String? = null) {
        viewModel.setLoading(true)
        lifecycleScope.launch {
            viewModel.searchRecipes(query, filters)
            viewModel.setLoading(false)
        }
    }

}
