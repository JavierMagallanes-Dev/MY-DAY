package com.example.myapplicationmyday.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSocialPlatform(platform: SocialPlatform): String {
        return platform.name
    }
    
    @TypeConverter
    fun toSocialPlatform(value: String): SocialPlatform {
        return try {
            SocialPlatform.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SocialPlatform.OTHER
        }
    }
}
