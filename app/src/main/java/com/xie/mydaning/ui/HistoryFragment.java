package com.xie.mydaning.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.xie.mydaning.R;
import com.xie.mydaning.data.PeriodRecord;
import com.xie.mydaning.utils.DateUtils;
import com.xie.mydaning.utils.PeriodCalculator;
import com.xie.mydaning.viewmodel.PeriodViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnItemActionListener {
    private PeriodViewModel viewModel;
    private Button btnFilterAll, btnFilterMonth, btnFilterYear;
    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private LineChart chartCycle;
    private String currentFilter = "all";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(PeriodViewModel.class);
        
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterMonth = view.findViewById(R.id.btn_filter_month);
        btnFilterYear = view.findViewById(R.id.btn_filter_year);
        rvHistory = view.findViewById(R.id.rv_history);
        chartCycle = view.findViewById(R.id.chart_cycle);
        
        adapter = new HistoryAdapter();
        adapter.setOnItemActionListener(this);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);
        
        // 筛选按钮
        btnFilterAll.setOnClickListener(v -> filterRecords("all"));
        btnFilterMonth.setOnClickListener(v -> filterRecords("month"));
        btnFilterYear.setOnClickListener(v -> filterRecords("year"));
        
        // 观察数据
        viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            filterRecords(currentFilter);
            updateChart();
        });
        
        setupChart();
    }
    
    private void filterRecords(String filter) {
        currentFilter = filter;
        
        btnFilterAll.setSelected(filter.equals("all"));
        btnFilterMonth.setSelected(filter.equals("month"));
        btnFilterYear.setSelected(filter.equals("year"));
        
        viewModel.getAllRecords().observe(getViewLifecycleOwner(), allRecords -> {
            if (allRecords == null) {
                adapter.setRecords(new ArrayList<>());
                return;
            }
            
            List<PeriodRecord> filtered = new ArrayList<>();
            Date now = new Date();
            
            for (PeriodRecord record : allRecords) {
                boolean include = false;
                
                if (filter.equals("all")) {
                    include = true;
                } else if (filter.equals("month")) {
                    Date monthStart = DateUtils.getStartOfMonth(now);
                    if (record.date.after(monthStart) || record.date.equals(monthStart)) {
                        include = true;
                    }
                } else if (filter.equals("year")) {
                    Date yearStart = DateUtils.getStartOfYear(now);
                    if (record.date.after(yearStart) || record.date.equals(yearStart)) {
                        include = true;
                    }
                }
                
                if (include) {
                    filtered.add(record);
                }
            }
            
            adapter.setRecords(filtered);
        });
    }
    
    private void setupChart() {
        chartCycle.getDescription().setEnabled(false);
        chartCycle.setTouchEnabled(true);
        chartCycle.setDragEnabled(true);
        chartCycle.setScaleEnabled(false);
        chartCycle.setPinchZoom(false);
        chartCycle.setDrawGridBackground(false);
        
        XAxis xAxis = chartCycle.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        
        chartCycle.getAxisLeft().setDrawGridLines(false);
        chartCycle.getAxisRight().setEnabled(false);
        chartCycle.getLegend().setEnabled(false);
    }
    
    private void updateChart() {
        viewModel.getAllStartRecords().observe(getViewLifecycleOwner(), startRecords -> {
            if (startRecords == null || startRecords.size() < 2) {
                chartCycle.clear();
                return;
            }
            
            List<PeriodCalculator.PeriodInfo> periods = new ArrayList<>();
            for (PeriodRecord record : startRecords) {
                periods.add(new PeriodCalculator.PeriodInfo(record.date, null));
            }
            
            if (periods.size() < 2) {
                chartCycle.clear();
                return;
            }
            
            List<Entry> entries = new ArrayList<>();
            List<Integer> cycles = new ArrayList<>();
            
            for (int i = 0; i < periods.size() - 1; i++) {
                Date currentStart = periods.get(i).startDate;
                Date nextStart = periods.get(i + 1).startDate;
                int cycleLength = DateUtils.getDaysBetween(nextStart, currentStart);
                if (cycleLength > 0 && cycleLength < 60) {
                    cycles.add(cycleLength);
                }
            }
            
            for (int i = 0; i < cycles.size(); i++) {
                entries.add(new Entry(i, cycles.get(i)));
            }
            
            if (entries.isEmpty()) {
                chartCycle.clear();
                return;
            }
            
            LineDataSet dataSet = new LineDataSet(entries, "周期");
            dataSet.setColor(getContext().getColor(R.color.primary_color));
            dataSet.setLineWidth(3f);
            dataSet.setCircleColor(getContext().getColor(R.color.primary_color));
            dataSet.setCircleRadius(5f);
            dataSet.setDrawValues(false);
            
            LineData lineData = new LineData(dataSet);
            chartCycle.setData(lineData);
            chartCycle.invalidate();
        });
    }

    @Override
    public void onEdit(PeriodRecord record) {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_record, null, false);

        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_status);
        RadioGroup rgFlow = dialogView.findViewById(R.id.rg_flow);
        RadioGroup rgPain = dialogView.findViewById(R.id.rg_pain);
        EditText etNotes = dialogView.findViewById(R.id.et_notes_edit);

        // 预选状态
        if ("start".equals(record.type)) {
            ((RadioButton) dialogView.findViewById(R.id.rb_status_start)).setChecked(true);
        } else if ("end".equals(record.type)) {
            ((RadioButton) dialogView.findViewById(R.id.rb_status_end)).setChecked(true);
        } else {
            ((RadioButton) dialogView.findViewById(R.id.rb_status_none)).setChecked(true);
        }

        // 预选流量
        if ("light".equals(record.flow)) {
            ((RadioButton) dialogView.findViewById(R.id.rb_flow_light)).setChecked(true);
        } else if ("heavy".equals(record.flow)) {
            ((RadioButton) dialogView.findViewById(R.id.rb_flow_heavy)).setChecked(true);
        } else {
            ((RadioButton) dialogView.findViewById(R.id.rb_flow_normal)).setChecked(true);
        }

        // 预选疼痛
        switch (record.pain) {
            case 1:
                ((RadioButton) dialogView.findViewById(R.id.rb_pain_1)).setChecked(true);
                break;
            case 2:
                ((RadioButton) dialogView.findViewById(R.id.rb_pain_2)).setChecked(true);
                break;
            case 3:
                ((RadioButton) dialogView.findViewById(R.id.rb_pain_3)).setChecked(true);
                break;
            default:
                ((RadioButton) dialogView.findViewById(R.id.rb_pain_0)).setChecked(true);
                break;
        }

        etNotes.setText(record.notes == null ? "" : record.notes);

        new AlertDialog.Builder(requireContext())
                .setTitle("编辑记录")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    record.type = getStatusValue(rgStatus.getCheckedRadioButtonId());
                    record.flow = getFlowValue(rgFlow.getCheckedRadioButtonId());
                    record.pain = getPainValue(rgPain.getCheckedRadioButtonId());
                    record.notes = etNotes.getText().toString();
                    viewModel.update(record);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onDelete(PeriodRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除记录")
                .setMessage("确定删除这条记录吗？")
                .setPositiveButton("删除", (dialog, which) -> viewModel.delete(record))
                .setNegativeButton("取消", null)
                .show();
    }

    private String getStatusValue(int id) {
        if (id == R.id.rb_status_start) return "start";
        if (id == R.id.rb_status_end) return "end";
        return "none";
    }

    private String getFlowValue(int id) {
        if (id == R.id.rb_flow_light) return "light";
        if (id == R.id.rb_flow_heavy) return "heavy";
        return "normal";
    }

    private int getPainValue(int id) {
        if (id == R.id.rb_pain_1) return 1;
        if (id == R.id.rb_pain_2) return 2;
        if (id == R.id.rb_pain_3) return 3;
        return 0;
    }
}

