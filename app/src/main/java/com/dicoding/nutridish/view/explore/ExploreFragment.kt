package com.dicoding.nutridish.view.explore

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.databinding.FragmentExploreBinding
import com.dicoding.nutridish.helper.ImageClassifierHelper
import com.google.android.material.bottomsheet.BottomSheetDialog

class ExploreFragment : Fragment() {

    private val viewModel: ExploreViewModel by viewModels{
        ViewModelFactory.getInstance(requireActivity())
    }
    private lateinit var recipeAdapter: ExploreAdapter
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val classLabels = listOf(
        "beans", "beef", "bell pepper", "bread", "butter", "cabbage", "carrot",
        "cheese", "chicken", "egg", "eggplant", "fish", "onion", "pasta",
        "peanut", "pork", "potato", "rice", "shrimp", "tofu", "tomato", "zucchini"
    )

    private var currentImageUri: Uri? = null

    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    currentImageUri = it
                    analyzeImage(it)
                }
            }
        }

    private val cameraLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentImageUri?.let {
                    currentImageUri = it
                    analyzeImage(it)
                }
            }
        }

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                openCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions are required to use the camera.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

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

        binding.capture.setOnClickListener {
            showImageSourceDialog()
        }

        recipeAdapter = ExploreAdapter { isLoading ->
            viewModel.setLoading(isLoading)
        }

        binding.recyclerViewSearch.apply {
            val gridLayoutManager = GridLayoutManager(context, 2)
            layoutManager = gridLayoutManager
            adapter = recipeAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val visibleItemCount = gridLayoutManager.childCount
                    val totalItemCount = gridLayoutManager.itemCount
                    val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        viewModel.loadMoreRecipes()
                    }
                }
            })
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

    private fun analyzeImage(image : Uri){
            // Initialize the ImageClassifierHelper here and classify the image
            val imageClassifierHelper = ImageClassifierHelper(
                context = requireContext(),
                classifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onError(error: String) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }

                    override fun onResults(results: FloatArray, inferenceTime: Long) {
                        if (results.isNotEmpty()) {
                            val maxIndex = results.indices.maxByOrNull { results[it] } ?: -1
                            if (maxIndex >= 0) {
                                val predictedLabel = classLabels[maxIndex]
                                // Pass the predicted label to ExploreFragment
                                searchRecipes(predictedLabel)

                            }
                        }
                    }
                }
            )

            // Trigger classification with the uploaded image URI
            imageClassifierHelper.classifyStaticImage(image)

    }

    private fun showImageSourceDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_capture_image, null)

        dialogView.findViewById<View>(R.id.btnCamera)?.setOnClickListener {
            dialog.dismiss()
            checkCameraPermissionAndOpenCamera()
        }

        dialogView.findViewById<View>(R.id.btnGallery)?.setOnClickListener {
            dialog.dismiss()
            openGallery()
        }

        dialog.setContentView(dialogView)
        dialog.show()
    }


    @SuppressLint("ObsoleteSdkInt")
    private fun checkCameraPermissionAndOpenCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}" + ".jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        currentImageUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        currentImageUri?.let {
            cameraLauncher.launch(it)
        }
        if (currentImageUri != null){
            analyzeImage(currentImageUri!!)
        }

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
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
        viewModel.searchRecipes(query, filters)
        Toast.makeText(requireContext(), "Searching for $query", Toast.LENGTH_SHORT).show()
    }



}
