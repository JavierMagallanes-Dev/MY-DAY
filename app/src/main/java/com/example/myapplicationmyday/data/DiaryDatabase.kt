package com.example.myapplicationmyday.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [DiaryEntry::class, DeletedEntry::class, SocialMediaLink::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun deletedEntryDao(): DeletedEntryDao
    abstract fun socialMediaDao(): SocialMediaDao
    
    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null
        
        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
