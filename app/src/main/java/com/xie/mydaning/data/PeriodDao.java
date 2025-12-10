package com.xie.mydaning.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface PeriodDao {
    @Query("SELECT * FROM period_records ORDER BY date DESC")
    LiveData<List<PeriodRecord>> getAllRecords();
    
    @Query("SELECT * FROM period_records WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    LiveData<List<PeriodRecord>> getRecordsByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT * FROM period_records WHERE type = 'start' ORDER BY date DESC")
    LiveData<List<PeriodRecord>> getAllStartRecords();
    
    @Query("SELECT * FROM period_records WHERE type = 'end' ORDER BY date DESC")
    LiveData<List<PeriodRecord>> getAllEndRecords();
    
    @Insert
    void insert(PeriodRecord record);
    
    @Update
    void update(PeriodRecord record);
    
    @Delete
    void delete(PeriodRecord record);
    
    @Query("DELETE FROM period_records")
    void deleteAll();
}

