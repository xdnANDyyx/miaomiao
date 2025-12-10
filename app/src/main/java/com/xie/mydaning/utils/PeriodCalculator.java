package com.xie.mydaning.utils;

import com.xie.mydaning.data.PeriodRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PeriodCalculator {
    
    public static class PeriodInfo {
        public Date startDate;
        public Date endDate;
        public int cycleLength; // 周期长度（天）
        public int periodLength; // 经期长度（天）
        
        public PeriodInfo(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            if (endDate != null) {
                this.periodLength = DateUtils.getDaysBetween(startDate, endDate) + 1;
            }
        }
    }
    
    /**
     * 计算平均周期长度
     */
    public static int calculateAverageCycle(List<PeriodRecord> startRecords) {
        if (startRecords == null || startRecords.size() < 2) {
            return 28; // 默认28天
        }
        
        List<PeriodInfo> periods = getPeriods(startRecords);
        if (periods.size() < 2) {
            return 28;
        }
        
        int totalDays = 0;
        int count = 0;
        for (int i = 0; i < periods.size() - 1; i++) {
            Date currentStart = periods.get(i).startDate;
            Date nextStart = periods.get(i + 1).startDate;
            int cycleLength = DateUtils.getDaysBetween(nextStart, currentStart);
            if (cycleLength > 0 && cycleLength < 60) { // 过滤异常数据
                totalDays += cycleLength;
                count++;
            }
        }
        
        return count > 0 ? totalDays / count : 28;
    }
    
    /**
     * 计算平均经期长度
     */
    public static int calculateAveragePeriod(List<PeriodRecord> startRecords, List<PeriodRecord> endRecords) {
        if (startRecords == null || startRecords.isEmpty()) {
            return 5; // 默认5天
        }
        
        List<PeriodInfo> periods = getPeriodsWithEnds(startRecords, endRecords);
        if (periods.isEmpty()) {
            return 5;
        }
        
        int totalDays = 0;
        int count = 0;
        for (PeriodInfo period : periods) {
            if (period.endDate != null && period.periodLength > 0 && period.periodLength < 15) {
                totalDays += period.periodLength;
                count++;
            }
        }
        
        return count > 0 ? totalDays / count : 5;
    }
    
    /**
     * 计算规律度
     */
    public static int calculateRegularity(List<PeriodRecord> startRecords) {
        if (startRecords == null || startRecords.size() < 3) {
            return 0;
        }
        
        List<PeriodInfo> periods = getPeriods(startRecords);
        if (periods.size() < 3) {
            return 0;
        }
        
        List<Integer> cycles = new ArrayList<>();
        for (int i = 0; i < periods.size() - 1; i++) {
            Date currentStart = periods.get(i).startDate;
            Date nextStart = periods.get(i + 1).startDate;
            int cycleLength = DateUtils.getDaysBetween(nextStart, currentStart);
            if (cycleLength > 0 && cycleLength < 60) {
                cycles.add(cycleLength);
            }
        }
        
        if (cycles.size() < 2) {
            return 0;
        }
        
        int average = cycles.stream().mapToInt(Integer::intValue).sum() / cycles.size();
        int variance = 0;
        for (int cycle : cycles) {
            variance += Math.abs(cycle - average);
        }
        variance = variance / cycles.size();
        
        // 规律度计算：方差越小，规律度越高
        int regularity = Math.max(0, 100 - variance * 5);
        return Math.min(100, regularity);
    }
    
    /**
     * 预测下次经期开始日期
     */
    public static Date predictNextPeriod(Date lastStartDate, int averageCycle) {
        return DateUtils.addDays(lastStartDate, averageCycle);
    }
    
    /**
     * 获取当前经期状态
     */
    public static class CurrentPeriodStatus {
        public boolean isActive;
        public Date startDate;
        public Date endDate;
        public int currentDay;
        public Date nextPeriodDate;
        
        public CurrentPeriodStatus(boolean isActive, Date startDate, Date endDate, int currentDay, Date nextPeriodDate) {
            this.isActive = isActive;
            this.startDate = startDate;
            this.endDate = endDate;
            this.currentDay = currentDay;
            this.nextPeriodDate = nextPeriodDate;
        }
    }
    
    public static CurrentPeriodStatus getCurrentPeriodStatus(List<PeriodRecord> startRecords, 
                                                             List<PeriodRecord> endRecords,
                                                             int averageCycle) {
        Date today = DateUtils.getStartOfDay(new Date());
        
        if (startRecords == null || startRecords.isEmpty()) {
            return new CurrentPeriodStatus(false, null, null, 0, null);
        }
        
        // 找到最近的开始记录
        PeriodRecord lastStart = startRecords.get(0);
        Date lastStartDate = DateUtils.getStartOfDay(lastStart.date);
        
        // 找到这个开始记录之后最近的结束记录
        PeriodRecord correspondingEnd = null;
        if (endRecords != null) {
            for (PeriodRecord end : endRecords) {
                Date endDate = DateUtils.getStartOfDay(end.date);
                if (endDate.after(lastStartDate) || endDate.equals(lastStartDate)) {
                    correspondingEnd = end;
                    break;
                }
            }
        }
        
        Date lastEndDate = correspondingEnd != null ? DateUtils.getStartOfDay(correspondingEnd.date) : null;
        
        boolean isActive = false;
        int currentDay = 0;
        
        if (lastEndDate == null || lastEndDate.before(lastStartDate)) {
            // 没有结束记录，或者结束记录在开始记录之前，说明经期可能还在进行
            if (today.after(lastStartDate) || today.equals(lastStartDate)) {
                isActive = true;
                currentDay = DateUtils.getDaysBetween(lastStartDate, today) + 1;
            }
        } else {
            // 有结束记录
            if (today.after(lastStartDate) && (today.before(lastEndDate) || today.equals(lastEndDate))) {
                isActive = true;
                currentDay = DateUtils.getDaysBetween(lastStartDate, today) + 1;
            }
        }
        
        Date nextPeriodDate = predictNextPeriod(lastStartDate, averageCycle);
        
        return new CurrentPeriodStatus(isActive, lastStartDate, lastEndDate, currentDay, nextPeriodDate);
    }
    
    private static List<PeriodInfo> getPeriods(List<PeriodRecord> startRecords) {
        List<PeriodInfo> periods = new ArrayList<>();
        for (PeriodRecord record : startRecords) {
            periods.add(new PeriodInfo(record.date, null));
        }
        return periods;
    }
    
    private static List<PeriodInfo> getPeriodsWithEnds(List<PeriodRecord> startRecords, List<PeriodRecord> endRecords) {
        List<PeriodInfo> periods = new ArrayList<>();
        
        for (PeriodRecord startRecord : startRecords) {
            Date startDate = startRecord.date;
            Date endDate = null;
            
            // 找到这个开始记录之后最近的结束记录
            if (endRecords != null) {
                for (PeriodRecord endRecord : endRecords) {
                    if (endRecord.date.after(startDate) || endRecord.date.equals(startDate)) {
                        endDate = endRecord.date;
                        break;
                    }
                }
            }
            
            periods.add(new PeriodInfo(startDate, endDate));
        }
        
        return periods;
    }
}

