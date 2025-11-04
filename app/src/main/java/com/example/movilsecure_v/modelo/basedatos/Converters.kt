package com.example.movilsecure_v.modelo.basedatos

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversores de tipos para que Room pueda manejar datos complejos como Date.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
