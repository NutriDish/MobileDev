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

    private val viewModel: ExploreViewModel by viewModels {
        ViewModelFactory.getInstance(requireActivity())
    }
    private lateinit var recipeAdapter: ExploreAdapter
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding

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
                    analyzeImage(it)
                }
            }
        }

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filterValues { !it }.keys

            if (deniedPermissions.isEmpty()) {
                openCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions required: ${deniedPermissions.joinToString(", ")}",
                    Toast.LENGTH_SHORT
                ).show()
                requestSpecificPermissions(deniedPermissions.toTypedArray())
            }
        }

    private fun requestSpecificPermissions(permissions: Array<String>) {
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let { binding ->
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
                            && firstVisibleItemPosition >= 0
                        ) {
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
            })
        }

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipes?.let {
                recipeAdapter.updateRecipes(it.filterNotNull())
            }
        }

        searchRecipes("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun analyzeImage(image: Uri) {
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
                            searchRecipes(predictedLabel)
                        }
                    }
                }
            }
        )

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

    private fun checkCameraPermissionAndOpenCamera() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestSpecificPermissions(permissionsToRequest.toTypedArray())
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        currentImageUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        currentImageUri?.let {
            cameraLauncher.launch(it)
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

        val filters = mapOf(
            R.id.filterBreakfast to "breakfast",
            R.id.filterLunch to "lunch",
            R.id.filterdinner to "dinner",
            R.id.filtersnack to "snack",
            R.id.filterdesert to "desert",
            R.id.filtervegetarian to "vegetarian",
            R.id.filtervegan to "vegan",
            R.id.filterglutenfree to "gluten free",
            R.id.filterdairyfree to "dairy free",
            R.id.filterpescatarian to "pescatarian",
            R.id.filterpaleo to "paleo",
            R.id.filterpeanutfree to "peanut free",
            R.id.filtersoyfree to "soy free",
            R.id.filterlowcal to "low cal",
            R.id.filterlowcholesterol to "low cholesterol",
            R.id.filterlowfat to "low fat",
            R.id.filterlowcarb to "low carb",
            R.id.filterlowsodium to "low sodium",
            R.id.filterfatfree to "fat free",
            R.id.filterlowsugar to "low sugar"
        )

        val applyButton = dialogView.findViewById<Button>(R.id.applyFiltersButton)
        applyButton.setOnClickListener {
            val selectedFilters = filters.filterKeys {
                dialogView.findViewById<CheckBox>(it)?.isChecked == true
            }.values

            dialog.dismiss()

            val query = binding?.searchView?.query.toString()
            searchRecipes(query, if (selectedFilters.isNotEmpty()) selectedFilters.joinToString(",") else null)
        }

        dialog.show()
    }

    private fun searchRecipes(query: String, filters: String? = null) {
        viewModel.searchRecipes(query, filters)
        Toast.makeText(requireContext(), "Searching for $query", Toast.LENGTH_SHORT).show()
    }
}
