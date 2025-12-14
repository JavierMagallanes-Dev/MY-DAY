package com.example.myapplicationmyday.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val date: Long = Date().time,
    val createdAt: Long = Date().time,
    val userId: String = "",  // Firebase user ID
    val firestoreId: String = ""  // Firestore document ID for sync
)
