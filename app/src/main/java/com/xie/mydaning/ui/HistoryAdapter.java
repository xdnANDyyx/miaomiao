package com.xie.mydaning.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xie.mydaning.R;
import com.xie.mydaning.data.PeriodRecord;
import com.xie.mydaning.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<PeriodRecord> records = new ArrayList<>();
    private OnItemActionListener actionListener;

    public interface OnItemActionListener {
        void onEdit(PeriodRecord record);
        void onDelete(PeriodRecord record);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.actionListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PeriodRecord record = records.get(position);
        holder.bind(record, actionListener);
    }
    
    @Override
    public int getItemCount() {
        return records.size();
    }
    
    public void setRecords(List<PeriodRecord> records) {
        this.records = records != null ? records : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDay;
        private TextView tvMonth;
        private TextView tvType;
        private LinearLayout llTags;
        private TextView tvNotes;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvMonth = itemView.findViewById(R.id.tv_month);
            tvType = itemView.findViewById(R.id.tv_type);
            llTags = itemView.findViewById(R.id.ll_tags);
            tvNotes = itemView.findViewById(R.id.tv_notes);
        }
        
        void bind(PeriodRecord record, OnItemActionListener listener) {
            tvDay.setText(DateUtils.formatDay(record.date));
            tvMonth.setText(DateUtils.formatMonth(record.date));
            
            // 设置类型
            String typeText = "";
            if ("start".equals(record.type)) {
                typeText = "经期开始";
            } else if ("end".equals(record.type)) {
                typeText = "经期结束";
            } else {
                typeText = "记录";
            }
            tvType.setText(typeText);
            
            // 清除标签
            llTags.removeAllViews();
            
            // 添加流量标签
            if (record.flow != null) {
                TextView flowTag = createTag(itemView.getContext(), getFlowText(record.flow), 
                    getFlowBgColor(record.flow), getFlowTextColor(record.flow));
                llTags.addView(flowTag);
            }
            
            // 添加疼痛标签
            TextView painTag = createTag(itemView.getContext(), getPainText(record.pain),
                getPainBgColor(record.pain), getPainTextColor(record.pain));
            llTags.addView(painTag);
            
            // 设置备注
            if (record.notes != null && !record.notes.isEmpty()) {
                tvNotes.setText(record.notes);
                tvNotes.setVisibility(View.VISIBLE);
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(record);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(record);
                }
                return true;
            });
        }
        
        private TextView createTag(android.content.Context context, String text, int bgColor, int textColor) {
            TextView tag = new TextView(context);
            tag.setText(text);
            tag.setPadding(12, 4, 12, 4);
            tag.setTextSize(12);
            tag.setTextColor(textColor);
            tag.setBackgroundColor(bgColor);
            tag.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            ((LinearLayout.LayoutParams) tag.getLayoutParams()).setMargins(0, 0, 8, 0);
            return tag;
        }
        
        private String getFlowText(String flow) {
            switch (flow) {
                case "light": return "少量";
                case "normal": return "正常";
                case "heavy": return "较多";
                default: return "正常";
            }
        }
        
        private int getFlowBgColor(String flow) {
            switch (flow) {
                case "light": return itemView.getContext().getColor(R.color.flow_light_bg);
                case "normal": return itemView.getContext().getColor(R.color.flow_normal_bg);
                case "heavy": return itemView.getContext().getColor(R.color.flow_heavy_bg);
                default: return itemView.getContext().getColor(R.color.flow_normal_bg);
            }
        }
        
        private int getFlowTextColor(String flow) {
            switch (flow) {
                case "light": return itemView.getContext().getColor(R.color.flow_light_text);
                case "normal": return itemView.getContext().getColor(R.color.flow_normal_text);
                case "heavy": return itemView.getContext().getColor(R.color.flow_heavy_text);
                default: return itemView.getContext().getColor(R.color.flow_normal_text);
            }
        }
        
        private String getPainText(int pain) {
            switch (pain) {
                case 0: return "无痛";
                case 1: return "轻微";
                case 2: return "中度";
                case 3: return "严重";
                default: return "无痛";
            }
        }
        
        private int getPainBgColor(int pain) {
            switch (pain) {
                case 0: return itemView.getContext().getColor(R.color.pain_none_bg);
                case 1: return itemView.getContext().getColor(R.color.pain_mild_bg);
                case 2: return itemView.getContext().getColor(R.color.pain_moderate_bg);
                case 3: return itemView.getContext().getColor(R.color.pain_severe_bg);
                default: return itemView.getContext().getColor(R.color.pain_none_bg);
            }
        }
        
        private int getPainTextColor(int pain) {
            switch (pain) {
                case 0: return itemView.getContext().getColor(R.color.pain_none_text);
                case 1: return itemView.getContext().getColor(R.color.pain_mild_text);
                case 2: return itemView.getContext().getColor(R.color.pain_moderate_text);
                case 3: return itemView.getContext().getColor(R.color.pain_severe_text);
                default: return itemView.getContext().getColor(R.color.pain_none_text);
            }
        }
    }
}

