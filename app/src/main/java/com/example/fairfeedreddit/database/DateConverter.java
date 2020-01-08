package com.example.fairfeedreddit.database;

import androidx.room.TypeConverter;

import java.util.Date;

class DateConverter {
    @TypeConverter
    static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
