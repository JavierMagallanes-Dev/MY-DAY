package com.example.myapplicationmyday.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SocialMediaDao {
    @Query("SELECT * FROM social_media_links ORDER BY createdAt DESC")
    fun getAllLinks(): LiveData<List<SocialMediaLink>>
    
    @Query("SELECT * FROM social_media_links WHERE platform = :platform ORDER BY createdAt DESC")
    fun getLinksByPlatform(platform: SocialPlatform): LiveData<List<SocialMediaLink>>
    
    @Query("SELECT * FROM social_media_links WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLinksByUserId(userId: String): LiveData<List<SocialMediaLink>>
    
    @Query("SELECT * FROM social_media_links WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getLinksByUserIdSync(userId: String): List<SocialMediaLink>
    
    @Query("SELECT * FROM social_media_links WHERE userId = :userId AND platform = :platform ORDER BY createdAt DESC")
    fun getLinksByUserAndPlatform(userId: String, platform: SocialPlatform): LiveData<List<SocialMediaLink>>
    
    @Query("SELECT * FROM social_media_links WHERE id = :id")
    suspend fun getLinkById(id: Long): SocialMediaLink?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: SocialMediaLink): Long
    
    @Update
    suspend fun update(link: SocialMediaLink)
    
    @Delete
    suspend fun delete(link: SocialMediaLink)
    
    @Query("DELETE FROM social_media_links WHERE id = :id")
    suspend fun deleteById(id: Long)
}
