package com.dicoding.nutridish.data.database.entity


import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Entity(tableName = "NutriDish")
@Parcelize
data class  NutriEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = "",
    var mediaCover: String? = null,
    @ColumnInfo("bookmarked")
    var isBookmarked: Boolean
) : Parcelable