package com.yanzhenjie.album.gpu;

import android.content.Context;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;



public class FitFrameLayout extends FrameLayout {

    private boolean isPortrait = true;

    public FitFrameLayout(@NonNull Context context) {
        super(context);
    }

    public FitFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FitFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        if (isPortrait) {
            setMeasuredDimension(width, width / 9 * 16);
        } else {
            setMeasuredDimension(width, getMeasuredHeight());
        }

    }
}

