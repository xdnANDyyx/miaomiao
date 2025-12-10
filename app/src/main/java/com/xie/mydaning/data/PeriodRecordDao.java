package com.xie.mydaning.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface PeriodRecordDao {
    @Insert
    void insert(PeriodRecord record);
    
    @Update
    void update(PeriodRecord record);
    
    @Query("SELECT * FROM period_records ORDER BY date DESC")
    LiveData<List<PeriodRecord>> getAllRecords();
    
    @Query("SELECT * FROM period_records WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    LiveData<List<PeriodRecord>> getRecordsByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT * FROM period_records WHERE type = 'start' ORDER BY date DESC")
    LiveData<List<PeriodRecord>> getAllStartRecords();
    
    @Query("SELECT * FROM period_records WHERE type = 'end' ORDER BY date DESC")
    LiveData<List<PeriodRecord>> getAllEndRecords();
    
    @Query("SELECT * FROM period_records WHERE date = (SELECT MAX(date) FROM period_records WHERE type = 'start')")
    PeriodRecord getLatestStartRecord();
    
    @Query("SELECT * FROM period_records WHERE date = (SELECT MAX(date) FROM period_records WHERE type = 'end')")
    PeriodRecord getLatestEndRecord();
}

