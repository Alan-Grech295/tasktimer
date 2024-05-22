package com.example.tasktimer.database.converters;

import androidx.room.TypeConverter;

import java.util.Date;


public class Converters {
    // Converts a long value from the database to a date value
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    // Converts a date object to a long value to be saved in the database
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
