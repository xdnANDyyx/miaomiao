package com.xie.mydaning.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeriodRepository {
    private PeriodDao periodDao;
    private LiveData<List<PeriodRecord>> allRecords;
    private ExecutorService executorService;
    
    public PeriodRepository(Application application) {
        PeriodDatabase database = PeriodDatabase.getDatabase(application);
        periodDao = database.periodDao();
        allRecords = periodDao.getAllRecords();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<PeriodRecord>> getAllRecords() {
        return allRecords;
    }
    
    public LiveData<List<PeriodRecord>> getRecordsByDateRange(Date startDate, Date endDate) {
        return periodDao.getRecordsByDateRange(startDate, endDate);
    }
    
    public LiveData<List<PeriodRecord>> getAllStartRecords() {
        return periodDao.getAllStartRecords();
    }
    
    public LiveData<List<PeriodRecord>> getAllEndRecords() {
        return periodDao.getAllEndRecords();
    }
    
    public void insert(PeriodRecord record) {
        executorService.execute(() -> periodDao.insert(record));
    }
    
    public void update(PeriodRecord record) {
        executorService.execute(() -> periodDao.update(record));
    }
    
    public void delete(PeriodRecord record) {
        executorService.execute(() -> periodDao.delete(record));
    }
}
