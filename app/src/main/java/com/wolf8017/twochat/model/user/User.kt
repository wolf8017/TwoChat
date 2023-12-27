package com.wolf8017.twochat.model.user

data class User(
    val userID: String? = null,
    val userName: String? = null,
    val userPhone: String? = null,
    val imageProfile: String? = null,
    val imageCover: String = "", // Provide a default value
    val email: String = "", // Provide a default value
    val dateOfBirth: String = "", // Provide a default value
    val gender: String = "",
    val status: String = "",
    val bio: String = "",
    val fcmToken: String = ""
)

