package com.arifahmadalfian.todolist.data.room

import androidx.room.TypeConverter
import com.arifahmadalfian.todolist.data.room.Priority

class Converter {

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }
}