package com.example.myapplicationmyday.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_media_links")
data class SocialMediaLink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val platform: SocialPlatform,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val userId: String = "",
    val firestoreId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class SocialPlatform(val displayName: String, val color: String) {
    FACEBOOK("Facebook", "#1877F2"),
    INSTAGRAM("Instagram", "#E4405F"),
    TIKTOK("TikTok", "#000000"),
    TWITTER("Twitter", "#1DA1F2"),
    YOUTUBE("YouTube", "#FF0000"),
    OTHER("Otro", "#6C757D")
}
