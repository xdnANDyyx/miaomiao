package com.xie.mydaning.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.xie.mydaning.data.PeriodRecord;
import com.xie.mydaning.data.PeriodRepository;
import com.xie.mydaning.utils.PeriodCalculator;

import java.util.Date;
import java.util.List;

public class PeriodViewModel extends AndroidViewModel {
    private PeriodRepository repository;
    private LiveData<List<PeriodRecord>> allRecords;
    private LiveData<List<PeriodRecord>> allStartRecords;
    private LiveData<List<PeriodRecord>> allEndRecords;
    
    private MutableLiveData<Integer> averageCycle = new MutableLiveData<>(28);
    private MutableLiveData<Integer> averagePeriod = new MutableLiveData<>(5);
    private MutableLiveData<Integer> regularity = new MutableLiveData<>(0);
    private MutableLiveData<PeriodCalculator.CurrentPeriodStatus> currentPeriodStatus = new MutableLiveData<>();
    
    public PeriodViewModel(Application application) {
        super(application);
        repository = new PeriodRepository(application);
        allRecords = repository.getAllRecords();
        allStartRecords = repository.getAllStartRecords();
        allEndRecords = repository.getAllEndRecords();
        
        // 观察数据变化，自动更新统计数据
        allStartRecords.observeForever(startRecords -> {
            if (startRecords != null) {
                updateStatistics(startRecords, allEndRecords.getValue());
            }
        });
        
        allEndRecords.observeForever(endRecords -> {
            if (endRecords != null) {
                updateStatistics(allStartRecords.getValue(), endRecords);
            }
        });
    }
    
    private void updateStatistics(List<PeriodRecord> startRecords, List<PeriodRecord> endRecords) {
        if (startRecords == null) {
            return;
        }
        
        int avgCycle = PeriodCalculator.calculateAverageCycle(startRecords);
        int avgPeriod = PeriodCalculator.calculateAveragePeriod(startRecords, endRecords);
        int reg = PeriodCalculator.calculateRegularity(startRecords);
        
        averageCycle.postValue(avgCycle);
        averagePeriod.postValue(avgPeriod);
        regularity.postValue(reg);
        
        // 更新当前经期状态
        PeriodCalculator.CurrentPeriodStatus status = 
            PeriodCalculator.getCurrentPeriodStatus(startRecords, endRecords, avgCycle);
        currentPeriodStatus.postValue(status);
    }
    
    public LiveData<List<PeriodRecord>> getAllRecords() {
        return allRecords;
    }
    
    public LiveData<List<PeriodRecord>> getAllStartRecords() {
        return allStartRecords;
    }
    
    public LiveData<List<PeriodRecord>> getAllEndRecords() {
        return allEndRecords;
    }
    
    public LiveData<Integer> getAverageCycle() {
        return averageCycle;
    }
    
    public LiveData<Integer> getAveragePeriod() {
        return averagePeriod;
    }
    
    public LiveData<Integer> getRegularity() {
        return regularity;
    }
    
    public LiveData<PeriodCalculator.CurrentPeriodStatus> getCurrentPeriodStatus() {
        return currentPeriodStatus;
    }
    
    public void insert(PeriodRecord record) {
        repository.insert(record);
    }
    
    public void update(PeriodRecord record) {
        repository.update(record);
    }
    
    public void delete(PeriodRecord record) {
        repository.delete(record);
    }
}
