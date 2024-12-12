package com.dicoding.nutridish.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dicoding.nutridish.data.database.FloatTypeConverter
import com.dicoding.nutridish.data.database.entity.NutriEntity
import com.dicoding.nutridish.data.database.entity.NotificationEntity

@Database(entities = [NutriEntity::class, NotificationEntity::class], version = 1, exportSchema = false)
@TypeConverters(FloatTypeConverter::class)
abstract class NutriDatabase : RoomDatabase() {
    abstract fun nutriDao(): NutriDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var instance: NutriDatabase? = null

        fun getInstance(context: Context): NutriDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NutriDatabase::class.java, "NutriDish.db"
                ).fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
