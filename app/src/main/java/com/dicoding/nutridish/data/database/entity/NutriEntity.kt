package com.dicoding.nutridish.data.database.entity


import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Entity(tableName = "NutriDish")
@Parcelize
data class NutriEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = "",
    var mediaCover: String? = null,
    var protein: @RawValue Any? = null,
    var fat: @RawValue Any? = null,
    var sodium: @RawValue Any? = null,
    var calories: @RawValue Any? = null,
    @ColumnInfo("bookmarked")
    var isBookmarked: Boolean
) : Parcelable

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long
)