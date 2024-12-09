package com.dicoding.nutridish.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.nutridish.data.database.entity.NutriEntity

@Database(entities = [NutriEntity::class], version = 1, exportSchema = false)
abstract class NutriDatabase : RoomDatabase() {
    abstract fun nutriDao(): NutriDao

    companion object {
        @Volatile
        private var instance: NutriDatabase? = null

        fun getInstance(context: Context): NutriDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NutriDatabase::class.java, "NutriDish.db"
                ).build().also { instance = it }
            }
        }
    }
}
