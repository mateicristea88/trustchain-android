package nl.tudelft.cs4160.trustchain_android.storage.database.converter;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    private DateConverter() {}

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
       return date == null ? null : date.getTime();
   }
}
