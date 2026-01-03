package net.cynreub.weighday.util;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateConverter {
    @TypeConverter
    public static LocalDateTime toLocalDateTime(String dateString) {
        return dateString == null ? null : LocalDateTime.parse(dateString);
    }

    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static LocalDate toLocalDate(String dateString) {
        return dateString == null ? null : LocalDate.parse(dateString);
    }

    @TypeConverter
    public static String fromLocalDate(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
