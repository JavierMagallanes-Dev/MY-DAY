package com.example.myapplicationmyday.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplicationmyday.data.DiaryDatabase
import com.example.myapplicationmyday.data.DiaryEntry
import com.example.myapplicationmyday.data.DiaryRepository
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DiaryRepository
    val allEntries: LiveData<List<DiaryEntry>>
    
    init {
        val diaryDao = DiaryDatabase.getDatabase(application).diaryDao()
        repository = DiaryRepository(diaryDao)
        allEntries = repository.allEntries
    }
    
    fun insert(entry: DiaryEntry) = viewModelScope.launch {
        repository.insert(entry)
    }
    
    fun update(entry: DiaryEntry) = viewModelScope.launch {
        repository.update(entry)
    }
    
    fun delete(entry: DiaryEntry) = viewModelScope.launch {
        repository.delete(entry)
    }
    
    fun syncFromFirestore(userId: String) = viewModelScope.launch {
        repository.syncFromFirestore(userId)
    }
}
