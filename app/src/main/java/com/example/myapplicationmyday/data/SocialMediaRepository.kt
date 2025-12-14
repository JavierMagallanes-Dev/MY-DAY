package com.example.myapplicationmyday.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class SocialMediaRepository(private val socialMediaDao: SocialMediaDao) {
    private val firestore: FirebaseFirestore = Firebase.firestore
    
    fun getAllLinks(): LiveData<List<SocialMediaLink>> = socialMediaDao.getAllLinks()
    
    fun getLinksByPlatform(platform: SocialPlatform): LiveData<List<SocialMediaLink>> =
        socialMediaDao.getLinksByPlatform(platform)
    
    fun getLinksByUserId(userId: String): LiveData<List<SocialMediaLink>> =
        socialMediaDao.getLinksByUserId(userId)
    
    fun getLinksByUserAndPlatform(userId: String, platform: SocialPlatform): LiveData<List<SocialMediaLink>> =
        socialMediaDao.getLinksByUserAndPlatform(userId, platform)
    
    suspend fun insert(link: SocialMediaLink): Long {
        val localId = socialMediaDao.insert(link)
        
        // Sync to Firestore
        if (link.userId.isNotEmpty()) {
            try {
                val docRef = firestore.collection("users")
                    .document(link.userId)
                    .collection("social_media_links")
                    .add(mapOf(
                        "url" to link.url,
                        "platform" to link.platform.name,
                        "title" to link.title,
                        "description" to link.description,
                        "imageUrl" to link.imageUrl,
                        "userId" to link.userId,
                        "createdAt" to link.createdAt
                    ))
                    .await()
                
                val updatedLink = link.copy(id = localId, firestoreId = docRef.id)
                socialMediaDao.update(updatedLink)
                Log.d("SocialMediaRepository", "Link synced to Firestore: ${docRef.id}")
            } catch (e: Exception) {
                Log.e("SocialMediaRepository", "Error syncing to Firestore", e)
            }
        }
        
        return localId
    }
    
    suspend fun update(link: SocialMediaLink) {
        socialMediaDao.update(link)
        
        if (link.userId.isNotEmpty() && link.firestoreId.isNotEmpty()) {
            try {
                firestore.collection("users")
                    .document(link.userId)
                    .collection("social_media_links")
                    .document(link.firestoreId)
                    .update(mapOf(
                        "url" to link.url,
                        "platform" to link.platform.name,
                        "title" to link.title,
                        "description" to link.description,
                        "imageUrl" to link.imageUrl
                    ))
                    .await()
                Log.d("SocialMediaRepository", "Link updated in Firestore")
            } catch (e: Exception) {
                Log.e("SocialMediaRepository", "Error updating Firestore", e)
            }
        }
    }
    
    suspend fun delete(link: SocialMediaLink) {
        socialMediaDao.delete(link)
        
        if (link.userId.isNotEmpty() && link.firestoreId.isNotEmpty()) {
            try {
                firestore.collection("users")
                    .document(link.userId)
                    .collection("social_media_links")
                    .document(link.firestoreId)
                    .delete()
                    .await()
                Log.d("SocialMediaRepository", "Link deleted from Firestore")
            } catch (e: Exception) {
                Log.e("SocialMediaRepository", "Error deleting from Firestore", e)
            }
        }
    }
    
    suspend fun syncFromFirestore(userId: String) {
        if (userId.isEmpty()) return
        
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("social_media_links")
                .get()
                .await()
            
            // Get existing firestoreIds from local database
            val existingLinks = socialMediaDao.getLinksByUserIdSync(userId)
            val existingFirestoreIds = existingLinks.mapNotNull { it.firestoreId }.toSet()
            
            snapshot.documents.forEach { doc ->
                // Skip if already exists locally
                if (doc.id in existingFirestoreIds) {
                    Log.d("SocialMediaRepository", "Link ${doc.id} already exists locally, skipping")
                    return@forEach
                }
                
                val platformName = doc.getString("platform") ?: "OTHER"
                val platform = try {
                    SocialPlatform.valueOf(platformName)
                } catch (e: Exception) {
                    SocialPlatform.OTHER
                }
                
                val link = SocialMediaLink(
                    id = 0,
                    url = doc.getString("url") ?: "",
                    platform = platform,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    userId = doc.getString("userId") ?: userId,
                    firestoreId = doc.id,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
                
                socialMediaDao.insert(link)
                Log.d("SocialMediaRepository", "Synced new link from Firestore: ${doc.id}")
            }
            
            Log.d("SocialMediaRepository", "Sync completed: ${snapshot.size()} links from Firestore")
        } catch (e: Exception) {
            Log.e("SocialMediaRepository", "Error syncing from Firestore", e)
        }
    }
}
