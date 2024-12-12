package com.dicoding.nutridish.data.database

import androidx.room.TypeConverter

class FloatTypeConverter {

    @TypeConverter
    fun fromAny(value: Any?): Float {
        return when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: 0f
            else -> 0f
        }
    }

    @TypeConverter
    fun toAny(value: Float?): Any? {
        return value
    }
}
