    @file:Suppress("DEPRECATION")

    package com.dicoding.nutridish.view.detail

    import android.annotation.SuppressLint
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.lifecycle.LifecycleOwner
    import androidx.lifecycle.LiveData
    import androidx.lifecycle.Observer
    import androidx.lifecycle.ViewModelProvider
    import androidx.lifecycle.lifecycleScope
    import com.dicoding.nutridish.R
    import com.dicoding.nutridish.ViewModelFactory
    import com.dicoding.nutridish.data.database.entity.NutriEntity
    import com.dicoding.nutridish.databinding.ActivityDetailBinding
    import kotlinx.coroutines.launch
    import com.dicoding.nutridish.data.Result
    import com.dicoding.nutridish.data.api.response.ResponseItem

    class DetailActivity : AppCompatActivity() {
        private lateinit var binding: ActivityDetailBinding
        private lateinit var database: NutriEntity
        private lateinit var viewModel: DetailViewModel
        val data = "recipe_data_list"
        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val factory = ViewModelFactory.getInstance(this)
            viewModel = ViewModelProvider(this, factory)[DetailViewModel::class.java]

            binding.backButton.setOnClickListener {
                finish()
            }

            val nutriItem: ResponseItem? = intent.getParcelableExtra(data)

            if (nutriItem != null) {
                showLoading(true)
                lifecycleScope.launch {
                    viewModel.getNutriDetail(
                        nutriItem.title ?: "Data Is Missing !"
                    )
                }

//                Log.d("DetailEventActivity", "Event data: $nutriItem") // log untuk lihat data

            } else {
                handleError()
            }

            val ivBookmark = binding.favoriteButton
            ivBookmark.setOnClickListener {
                updateBookmarkIcon(database)
            }

            viewModel.nutriDetail.observe(this) { result ->
                when (result) {
                    is Result.Error -> {
                        showLoading(false)
                        handleError()
                    }
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Success -> {
                        showLoading(false)
                        val data = result.data

                        // Set the dish title
                        binding.dishTitle.text = data.title ?: "Default Recipe"

                        // Set the description
                        binding.descriptionText.text = data.description ?: "No Description"
                        // Set the ingredients list
                        binding.ingredientsText.text = data.ingredients?.joinToString("\n") ?: "No Ingredients"

                        binding.instructionsText.text = data.directions?.joinToString("\n") ?: "No Instructions"

                        // Update nutrition card data
                        binding.caloriestext.text = data.calories?.toString() ?: "0"
                        binding.proteintext.text = data.protein?.toString() ?: "0"
                        binding.fattext.text = data.fat?.toString() ?: "0"
                        binding.sodiumtext.text = data.sodium?.toString() ?: "0"

                        // Initialize NutriEntity
                        if (nutriItem != null) {
                            database = NutriEntity(
                                title = nutriItem.title ?: "Data Is Missing!",
                                mediaCover = null,
                                calories = data.calories ?: 0,
                                protein = data.protein ?: 0,
                                fat = data.fat ?: 0,
                                sodium = data.sodium ?: 0,
                                isBookmarked = true
                            )
                        }
                        viewModel.checkBookmark(database.title.trim()).observe(this) { eventEntity ->
                            if (eventEntity != null) {
                                binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_24)
                            } else {
                                binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
                            }
                        }
                    }
                }
            }
        }



        private fun updateBookmarkIcon(database: NutriEntity) {
            viewModel.checkBookmark(database.title).observeOnce(this) { isBookmarked ->
                if (isBookmarked != null) {
                    viewModel.deleteBookmark(database.title)
                    Toast.makeText(
                        this@DetailActivity,
                        "Delete Favorite Recipe Success",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                        viewModel.setBookmark(database)
                        Toast.makeText(
                            this@DetailActivity,
                            "Save Favorite Recipe Success",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

        }
        private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
            val wrapper = object : Observer<T> {
                override fun onChanged(value: T) {
                    observer.onChanged(value)
                    removeObserver(this)
                }
            }
            observe(owner, wrapper)
        }


        private fun showLoading(isLoading: Boolean) {
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        private fun handleError() {
            val message = "Data Is Missing !"
            showLoading(false)
            binding.dishTitle.text = message
        }
    }