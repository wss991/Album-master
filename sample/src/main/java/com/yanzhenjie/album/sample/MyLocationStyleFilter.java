package com.yanzhenjie.album.sample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.daasuu.gpuv.egl.filter.GlOverlayFilter;

public class MyLocationStyleFilter extends GlOverlayFilter {

    private Bitmap bitmap;
    private Position position = Position.LEFT_TOP;

    String te = "经纬度：30.123456°N，119.987654°E\n地\u3000点：中国浙江省杭州市富阳区234国道辅路中国智谷富春园区\n时\u3000间：2023.02.26 10:08";
    private Paint textPaint;
    private TextPaint myPaint;
    private Paint rentPaint;
    public MyLocationStyleFilter(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public MyLocationStyleFilter(Bitmap bitmap, Position position) {
//        this.bitmap = bitmap;
        this.position = position;

        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        myPaint = new TextPaint();
        myPaint.setAntiAlias(true);
        myPaint.setTextSize(30);
        myPaint.setColor(Color.WHITE);
        rentPaint = new Paint();
        rentPaint.setStyle(Paint.Style.FILL);
        rentPaint.setColor(Color.YELLOW);
//        createQRCode();
       // this.bitmap = createImage("http://www.baidu.com","扫码导航");
    }


    @Override
    protected void drawCanvas(Canvas canvas) {
        Log.i("GlWatermarkFilter","帧水印");
        if (bitmap != null && !bitmap.isRecycled()) {
            switch (position) {
                case LEFT_TOP:
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    break;
                case LEFT_BOTTOM:
                    canvas.drawBitmap(bitmap, 0, canvas.getHeight() - bitmap.getHeight(), null);
                    break;
                case RIGHT_TOP:
                    canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth(), 0, null);
                    break;
                case RIGHT_BOTTOM:
//                    canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth()-50, 50, null);
                    canvas.drawBitmap(bitmap, canvas.getWidth()/2, 50, null);

//                    canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth(), canvas.getHeight() - bitmap.getHeight(), null);
                    StaticLayout layout = new StaticLayout(te,myPaint,canvas.getWidth()-bitmap.getWidth()-20, Layout.Alignment.ALIGN_NORMAL,1,0,false);
                    canvas.drawRect(10,canvas.getHeight()-layout.getHeight()+10-50,20,canvas.getHeight()-10-50,rentPaint);
                    canvas.translate(30,canvas.getHeight()-layout.getHeight()-50);
                    layout.draw(canvas);
//                    canvas.save();
//                    canvas.restore();
//                    canvas.drawText("30.123456°N，119.987654°E",canvas.getWidth()-textPaint.measureText("30.123456°N，119.987654°E"),canvas.getHeight()-bitmap.getHeight()-100,textPaint);
                    break;
            }
        }
    }

    public enum Position {
        LEFT_TOP,
        LEFT_BOTTOM,
        RIGHT_TOP,
        RIGHT_BOTTOM
    }


}
