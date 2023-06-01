package com.yanzhenjie.album.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by Android Studio.
 * User: admin
 * Date: 2023/5/23
 * Time: 15:08
 */
public class CaptureView extends android.support.v7.widget.AppCompatTextView {

    public CaptureView(Context context) {
        super(context);
    }

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint= new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        canvas.drawCircle(getWidth()/2,getHeight()/2,getWidth()/2-5,paint);

        Paint paint2= new Paint();
        paint2.setColor(Color.WHITE);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setAntiAlias(true);
        canvas.drawCircle(getWidth()/2,getHeight()/2,getWidth()/2-15,paint2);
    }
}
