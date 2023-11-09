package com.wolf8017.twochat.model.user

data class User(
    val userID: String,
    val userName: String,
    val userPhone: String,
    val imageProfile: String,
    val imageCover: String,
    val email: String,
    val dateOfBirth: String,
    val gender: String,
    val status: String,
    val bio: String,
)

