package nl.tudelft.cs4160.trustchain_android.storage.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import nl.tudelft.cs4160.trustchain_android.storage.database.converter.DateConverter;
import nl.tudelft.cs4160.trustchain_android.storage.database.dao.BlockDao;
import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbBlock;

@Database(entities = {DbBlock.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "trustchain";
    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public abstract BlockDao blockDao();
}
