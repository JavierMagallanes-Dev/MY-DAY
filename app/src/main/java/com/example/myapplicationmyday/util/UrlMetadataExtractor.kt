package com.example.myapplicationmyday.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class UrlMetadata(
    val title: String = "",
    val description: String = "",
    val imageUrl: String = ""
)

object UrlMetadataExtractor {
    
    suspend fun extractMetadata(url: String): UrlMetadata = withContext(Dispatchers.IO) {
        try {
            Log.d("UrlMetadataExtractor", "Extracting metadata from: $url")
            
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            // Try Open Graph tags first (used by Facebook, Instagram, etc.)
            val ogTitle = doc.select("meta[property=og:title]").attr("content")
            val ogDescription = doc.select("meta[property=og:description]").attr("content")
            val ogImage = doc.select("meta[property=og:image]").attr("content")
            
            // Fallback to Twitter Card tags
            val twitterTitle = doc.select("meta[name=twitter:title]").attr("content")
            val twitterDescription = doc.select("meta[name=twitter:description]").attr("content")
            val twitterImage = doc.select("meta[name=twitter:image]").attr("content")
            
            // Fallback to standard meta tags
            val metaDescription = doc.select("meta[name=description]").attr("content")
            val pageTitle = doc.title()
            
            val title = ogTitle.ifEmpty { twitterTitle.ifEmpty { pageTitle } }
            val description = ogDescription.ifEmpty { twitterDescription.ifEmpty { metaDescription } }
            val imageUrl = ogImage.ifEmpty { twitterImage }
            
            Log.d("UrlMetadataExtractor", "Extracted - Title: $title, Image: $imageUrl")
            
            UrlMetadata(
                title = title.take(200), // Limit to 200 chars
                description = description.take(300), // Limit to 300 chars
                imageUrl = imageUrl
            )
        } catch (e: Exception) {
            Log.e("UrlMetadataExtractor", "Error extracting metadata", e)
            UrlMetadata()
        }
    }
}
