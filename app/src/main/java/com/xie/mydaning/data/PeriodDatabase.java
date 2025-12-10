package com.xie.mydaning.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {PeriodRecord.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class PeriodDatabase extends RoomDatabase {
    public abstract PeriodDao periodDao();
    
    private static volatile PeriodDatabase INSTANCE;
    
    public static PeriodDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PeriodDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PeriodDatabase.class, "period_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

