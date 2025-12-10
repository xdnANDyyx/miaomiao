package com.xie.mydaning.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "period_records")
@TypeConverters(DateConverter.class)
public class PeriodRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public Date date;
    public String type; // "start", "end", "none"
    public String flow; // "light", "normal", "heavy"
    public int pain; // 0: 无痛, 1: 轻微, 2: 中度, 3: 严重
    public String notes;
    
    public PeriodRecord() {
    }
    
    @Ignore
    public PeriodRecord(Date date, String type, String flow, int pain, String notes) {
        this.date = date;
        this.type = type;
        this.flow = flow;
        this.pain = pain;
        this.notes = notes;
    }
}

