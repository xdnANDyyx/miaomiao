package com.xie.mydaning.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.xie.mydaning.MainActivity;
import com.xie.mydaning.R;
import com.xie.mydaning.data.PeriodRecord;
import com.xie.mydaning.viewmodel.PeriodViewModel;

import java.util.Date;

public class RecordFragment extends Fragment {
    private PeriodViewModel viewModel;
    private Button btnStatusStart, btnStatusEnd, btnStatusNone;
    private Button btnFlowLight, btnFlowNormal, btnFlowHeavy;
    private Button btnPain0, btnPain1, btnPain2, btnPain3;
    private EditText etNotes;
    private Button btnSave;
    
    private String selectedStatus = null;
    private String selectedFlow = null;
    private Integer selectedPain = null;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(PeriodViewModel.class);
        
        btnStatusStart = view.findViewById(R.id.btn_status_start);
        btnStatusEnd = view.findViewById(R.id.btn_status_end);
        btnStatusNone = view.findViewById(R.id.btn_status_none);
        btnFlowLight = view.findViewById(R.id.btn_flow_light);
        btnFlowNormal = view.findViewById(R.id.btn_flow_normal);
        btnFlowHeavy = view.findViewById(R.id.btn_flow_heavy);
        btnPain0 = view.findViewById(R.id.btn_pain_0);
        btnPain1 = view.findViewById(R.id.btn_pain_1);
        btnPain2 = view.findViewById(R.id.btn_pain_2);
        btnPain3 = view.findViewById(R.id.btn_pain_3);
        etNotes = view.findViewById(R.id.et_notes);
        btnSave = view.findViewById(R.id.btn_save);
        
        // 状态按钮
        btnStatusStart.setOnClickListener(v -> selectStatus("start", btnStatusStart));
        btnStatusEnd.setOnClickListener(v -> selectStatus("end", btnStatusEnd));
        btnStatusNone.setOnClickListener(v -> selectStatus("none", btnStatusNone));
        
        // 流量按钮
        btnFlowLight.setOnClickListener(v -> selectFlow("light", btnFlowLight));
        btnFlowNormal.setOnClickListener(v -> selectFlow("normal", btnFlowNormal));
        btnFlowHeavy.setOnClickListener(v -> selectFlow("heavy", btnFlowHeavy));
        
        // 疼痛按钮
        btnPain0.setOnClickListener(v -> selectPain(0, btnPain0));
        btnPain1.setOnClickListener(v -> selectPain(1, btnPain1));
        btnPain2.setOnClickListener(v -> selectPain(2, btnPain2));
        btnPain3.setOnClickListener(v -> selectPain(3, btnPain3));
        
        // 保存按钮
        btnSave.setOnClickListener(v -> saveRecord());
    }
    
    private void selectStatus(String status, Button button) {
        selectedStatus = status;
        btnStatusStart.setSelected(false);
        btnStatusEnd.setSelected(false);
        btnStatusNone.setSelected(false);
        button.setSelected(true);
    }
    
    private void selectFlow(String flow, Button button) {
        selectedFlow = flow;
        btnFlowLight.setSelected(false);
        btnFlowNormal.setSelected(false);
        btnFlowHeavy.setSelected(false);
        button.setSelected(true);
    }
    
    private void selectPain(int pain, Button button) {
        selectedPain = pain;
        btnPain0.setSelected(false);
        btnPain1.setSelected(false);
        btnPain2.setSelected(false);
        btnPain3.setSelected(false);
        button.setSelected(true);
    }
    
    private void saveRecord() {
        if (selectedStatus == null && selectedFlow == null && selectedPain == null) {
            Toast.makeText(getContext(), getString(R.string.please_select), Toast.LENGTH_SHORT).show();
            return;
        }
        
        PeriodRecord record = new PeriodRecord();
        record.date = new Date();
        record.type = selectedStatus != null ? selectedStatus : "none";
        record.flow = selectedFlow != null ? selectedFlow : "normal";
        record.pain = selectedPain != null ? selectedPain : 0;
        record.notes = etNotes.getText().toString();
        
        viewModel.insert(record);
        
        Toast.makeText(getContext(), getString(R.string.record_saved), Toast.LENGTH_SHORT).show();
        
        // 重置表单
        selectedStatus = null;
        selectedFlow = null;
        selectedPain = null;
        btnStatusStart.setSelected(false);
        btnStatusEnd.setSelected(false);
        btnStatusNone.setSelected(false);
        btnFlowLight.setSelected(false);
        btnFlowNormal.setSelected(false);
        btnFlowHeavy.setSelected(false);
        btnPain0.setSelected(false);
        btnPain1.setSelected(false);
        btnPain2.setSelected(false);
        btnPain3.setSelected(false);
        etNotes.setText("");
        
        // 返回首页
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).switchToHome();
        }
    }
}

