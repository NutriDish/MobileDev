package com.dicoding.nutridish.data.pref

data class UserModel(
    val email: String,
    val password: String,
    val isLogin: Boolean = false
)