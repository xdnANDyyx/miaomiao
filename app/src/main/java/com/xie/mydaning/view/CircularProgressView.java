package com.xie.mydaning.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.xie.mydaning.R;

public class CircularProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF rectF;
    private float progress = 0f;
    private String progressText = "0%";
    
    public CircularProgressView(Context context) {
        super(context);
        init();
    }
    
    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.card_bg_light));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(8f);
        
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_color));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(8f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary_color));
        textPaint.setTextSize(20f * getResources().getDisplayMetrics().density);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setFakeBoldText(true);
        
        rectF = new RectF();
    }
    
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(100f, progress));
        this.progressText = String.format("%.0f%%", this.progress);
        invalidate();
    }
    
    public void setProgressText(String text) {
        this.progressText = text;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 8f;
        
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        
        // 绘制背景圆
        canvas.drawArc(rectF, -90f, 360f, false, backgroundPaint);
        
        // 绘制进度弧
        float sweepAngle = (progress / 100f) * 360f;
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint);
        
        // 绘制文本
        float textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(progressText, centerX, textY, textPaint);
    }
}

