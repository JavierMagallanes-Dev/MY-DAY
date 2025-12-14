package com.example.myapplicationmyday.data

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
