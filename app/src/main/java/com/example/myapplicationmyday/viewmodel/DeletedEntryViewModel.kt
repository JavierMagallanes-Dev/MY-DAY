package com.example.myapplicationmyday.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplicationmyday.data.DeletedEntry
import com.example.myapplicationmyday.data.DiaryDatabase
import kotlinx.coroutines.launch

class DeletedEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val deletedEntryDao = DiaryDatabase.getDatabase(application).deletedEntryDao()
    val allDeletedEntries: LiveData<List<DeletedEntry>> = deletedEntryDao.getAllDeletedEntries()
    
    fun insert(entry: DeletedEntry) = viewModelScope.launch {
        deletedEntryDao.insert(entry)
    }
    
    fun delete(entry: DeletedEntry) = viewModelScope.launch {
        deletedEntryDao.delete(entry)
    }
    
    fun deleteOldEntries(thirtyDaysAgo: Long) = viewModelScope.launch {
        deletedEntryDao.deleteOldEntries(thirtyDaysAgo)
    }
    
    fun deleteAll() = viewModelScope.launch {
        deletedEntryDao.deleteAll()
    }
}
