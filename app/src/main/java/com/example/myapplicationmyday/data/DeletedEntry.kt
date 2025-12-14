package com.example.myapplicationmyday.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "deleted_entries")
data class DeletedEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalId: Long,
    val title: String,
    val content: String,
    val date: Long,
    val createdAt: Long,
    val deletedAt: Long = System.currentTimeMillis()
)

@Dao
interface DeletedEntryDao {
    @Query("SELECT * FROM deleted_entries ORDER BY deletedAt DESC")
    fun getAllDeletedEntries(): LiveData<List<DeletedEntry>>
    
    @Query("SELECT * FROM deleted_entries WHERE deletedAt < :thirtyDaysAgo")
    suspend fun getOldDeletedEntries(thirtyDaysAgo: Long): List<DeletedEntry>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DeletedEntry): Long
    
    @Delete
    suspend fun delete(entry: DeletedEntry)
    
    @Query("DELETE FROM deleted_entries WHERE deletedAt < :thirtyDaysAgo")
    suspend fun deleteOldEntries(thirtyDaysAgo: Long)
    
    @Query("DELETE FROM deleted_entries")
    suspend fun deleteAll()
}
