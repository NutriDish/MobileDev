package com.dicoding.nutridish.view.explore.upload

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.data.Result
import com.dicoding.nutridish.data.pref.UserPreference
import com.dicoding.nutridish.data.pref.dataStore
import com.dicoding.nutridish.databinding.ActivityUploadImageBinding
import com.dicoding.nutridish.getImageUri
import com.dicoding.nutridish.view.HomeActivity
import com.dicoding.nutridish.reduceFileImage
import com.dicoding.nutridish.uriToFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UploadImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadImageBinding
    private var currentImageUri: Uri? = null

    private lateinit var viewModel: UploadImageViewModel


    private val requestPermissionLauncer = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
        }

    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadImageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (!allPermissionsGranted()) {
            requestPermissionLauncer.launch(REQUIRED_PERMISSION)
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.idButtonAdd.setOnClickListener { uploadImage() }

        val factory = ViewModelFactory.getInstance(this@UploadImageActivity)
        viewModel = ViewModelProvider(this@UploadImageActivity, factory)[UploadImageViewModel::class.java]
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            lifecycleScope.launch {
                val imageFile = uriToFile(uri, this@UploadImageActivity).reduceFileImage()
                Log.d("Image File", "showImage: ${imageFile.path}")

                viewModel.uploadImage( imageFile).observe(this@UploadImageActivity) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                            }

                            is Result.Success -> {
                                result.data.message?.let { showToast(it) }
                                showLoading(false)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                finish()
                            }

                            is Result.Error -> {
                                showToast(result.error)
                                showLoading(false)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(this, "No Media Selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Toast.makeText(this, "Show Image", Toast.LENGTH_SHORT).show()
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}