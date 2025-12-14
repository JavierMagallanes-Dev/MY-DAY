package com.example.myapplicationmyday.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY date DESC, createdAt DESC")
    fun getAllEntries(): LiveData<List<DiaryEntry>>
    
    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): DiaryEntry?
    
    @Query("SELECT * FROM diary_entries WHERE userId = :userId")
    suspend fun getEntriesByUserId(userId: String): List<DiaryEntry>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntry): Long
    
    @Update
    suspend fun update(entry: DiaryEntry)
    
    @Delete
    suspend fun delete(entry: DiaryEntry)
    
    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
