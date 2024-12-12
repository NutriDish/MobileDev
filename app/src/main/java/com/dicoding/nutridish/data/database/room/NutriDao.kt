package com.dicoding.nutridish.data.database.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.nutridish.data.database.entity.NotificationEntity
import com.dicoding.nutridish.data.database.entity.NutriEntity

@Dao
interface NutriDao   {

    @Query("SELECT * FROM NutriDish WHERE bookmarked = 1")
    fun getBookmarkedNutri(): LiveData<List<NutriEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNutri(event: NutriEntity)

    @Query("DELETE FROM NutriDish WHERE bookmarked = 0")
    fun deleteAll()

    @Delete
    fun deleteNutri(nutriDish: NutriEntity)

    @Query("DELETE FROM NutriDish WHERE title = :title AND bookmarked = 1")
    fun deleteNutriById(title: String)


    @Query("SELECT EXISTS(SELECT * FROM NutriDish WHERE id = :id AND bookmarked = 1)")
    fun isNutriBookmarked(id: String): Boolean

    @Query("SELECT * FROM NutriDish WHERE title = :id")
    fun getFavoriteNutriById(id: String): LiveData<NutriEntity?>
}

@Dao
interface NotificationDao {

    @Insert
    fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): LiveData<List<NotificationEntity>>

    @Query("DELETE FROM notifications")
    fun clearNotifications()
}