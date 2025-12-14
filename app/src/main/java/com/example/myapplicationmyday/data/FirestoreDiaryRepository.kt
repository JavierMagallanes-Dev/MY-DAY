package com.example.myapplicationmyday.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreDiaryRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    private fun getUserId(): String? = auth.currentUser?.uid
    
    private fun getUserDiariesCollection() = 
        firestore.collection("users")
            .document(getUserId() ?: "")
            .collection("diaries")
    
    suspend fun saveDiaryEntry(entry: DiaryEntry): String {
        val userId = getUserId() ?: throw Exception("User not authenticated")
        
        val diaryData = hashMapOf(
            "title" to entry.title,
            "content" to entry.content,
            "date" to entry.date,
            "createdAt" to entry.createdAt,
            "userId" to userId
        )
        
        return if (entry.firestoreId.isEmpty()) {
            // Create new entry
            val docRef = getUserDiariesCollection().add(diaryData).await()
            docRef.id
        } else {
            // Update existing entry
            getUserDiariesCollection()
                .document(entry.firestoreId)
                .set(diaryData)
                .await()
            entry.firestoreId
        }
    }
    
    suspend fun getDiaryEntries(): List<DiaryEntry> {
        val userId = getUserId() ?: return emptyList()
        
        val snapshot = getUserDiariesCollection()
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            DiaryEntry(
                id = 0, // Room ID not used for Firestore entries
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                date = doc.getLong("date") ?: 0L,
                createdAt = doc.getLong("createdAt") ?: 0L,
                userId = userId,
                firestoreId = doc.id
            )
        }
    }
    
    suspend fun deleteDiaryEntry(firestoreId: String) {
        getUserDiariesCollection()
            .document(firestoreId)
            .delete()
            .await()
    }
    
    suspend fun updateDiaryEntry(entry: DiaryEntry) {
        if (entry.firestoreId.isEmpty()) return
        
        val diaryData = hashMapOf(
            "title" to entry.title,
            "content" to entry.content,
            "date" to entry.date,
            "createdAt" to entry.createdAt
        )
        
        getUserDiariesCollection()
            .document(entry.firestoreId)
            .update(diaryData as Map<String, Any>)
            .await()
    }
}
