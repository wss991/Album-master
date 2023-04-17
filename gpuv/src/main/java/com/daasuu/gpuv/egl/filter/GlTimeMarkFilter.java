package com.daasuu.gpuv.egl.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GlTimeMarkFilter extends GlOverlayFilter {

    private Bitmap bitmap;
    private Position position = Position.LEFT_TOP;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
    public GlTimeMarkFilter() {
    }


    public GlTimeMarkFilter( Position position) {
        this.position = position;
    }

    @Override
    protected void drawCanvas(Canvas canvas) {
        Log.i("GlWatermarkFilter","帧水印");
        updateWatermark();
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
                    canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth(), canvas.getHeight() - bitmap.getHeight(), null);
                    break;
            }
        }
    }
    private String lastTime="";
    private void updateWatermark() {
        String now= format.format(new Date());
        if (!lastTime.equals(now)){
            bitmap = getTimeBitmap();
            lastTime = now;
        }
    }

    public enum Position {
        LEFT_TOP,
        LEFT_BOTTOM,
        RIGHT_TOP,
        RIGHT_BOTTOM
    }

    private Bitmap getTimeBitmap(){
        String timeString = format.format(new Date());
        Paint paint = new Paint();
        paint.setTextSize(50.0F);
        paint.setColor(Color.parseColor("#000000"));
        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        int height = 200;
        Rect rect = new Rect();
        paint.getTextBounds(timeString, 0, timeString.length(), rect);
//            paint.setTypeface(Typeface.DEFAULT_BOLD);
        int width = rect.width() + 20;
        Bitmap bitmap = Bitmap.createBitmap(width,rect.height()+20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(timeString, 0.0F, -((float)metrics.ascent), paint);
        canvas.save();
        return bitmap;
    }
}
