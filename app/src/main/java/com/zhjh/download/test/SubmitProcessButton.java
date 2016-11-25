package com.zhjh.download.test;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;


public class SubmitProcessButton extends ProcessButton {

    public SubmitProcessButton(Context context) {
        super(context);
    }

    public SubmitProcessButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubmitProcessButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void drawProgress(Canvas canvas) {
        float scale = (float) getProgress() / (float) getMaxProgress();
        float indicatorWidth = (float) getMeasuredWidth() * scale;

        getProgressDrawable().setBounds(0, 0, (int) indicatorWidth, getMeasuredHeight());
        getProgressDrawable().draw(canvas);

        getEmptyDrawable().setBounds((int) indicatorWidth, 0, getMeasuredWidth(), getMeasuredHeight());
        getEmptyDrawable().draw(canvas);
    }

}
