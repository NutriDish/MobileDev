package com.dicoding.nutridish.view.explore.upload

import androidx.lifecycle.ViewModel
import com.dicoding.nutridish.data.UserRepository
import java.io.File

class UploadImageViewModel (val repository: UserRepository) : ViewModel() {
    fun uploadImage(file: File) = repository.uploadImage(file)

}