package com.kevalpatel2106.rulerpicker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class Picker {

    public static final int RADIUS = 25;
    public static final int DEFAULT_MARGIN = 10;
    public static final int RECT_RADIUS = 5;

    int startX, endX;
    int startY, endY;
    int width, height;

    int rulerValue;
    String timeValue;

    boolean startPicker;

    Paint paint;
    Paint bitmapPaint;

    public Picker(boolean startPicker) {
        this.startPicker = startPicker;
        configurePaint();
    }

    public void configurePaint() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(15);

        bitmapPaint = new Paint();
        bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bitmapPaint.setColor(Color.RED);
        bitmapPaint.setStrokeWidth(2f);
    }

    public void draw(Canvas canvas, View parent, int centerX, int centerY) {
        this.draw(canvas,parent, centerX, centerY, false);
    }

    public void draw(Canvas canvas, View parent, int centerX, int centerY, boolean drawTime) {

        this.startY = DEFAULT_MARGIN + RADIUS;

        startX = centerX - RADIUS;
        endX = centerX + RADIUS;
        endY = startY + RADIUS;

        width = endX - startX;
        height = endY - startY;

        RectF box = new RectF(centerX - RADIUS, startY - RADIUS, centerX + RADIUS, startY + RADIUS);
        canvas.drawRoundRect(box, RECT_RADIUS, RECT_RADIUS, bitmapPaint);
        canvas.drawLine(centerX, startY, centerX, centerY, bitmapPaint);


//        canvas.drawBitmap(b, startX, startY, bitmapPaint);
//        canvas.drawCircle(startX, startY + RADIUS, RADIUS, bitmapPaint);
//        RectF rectF = new RectF(centerX - RADIUS, startY - RADIUS, centerX + RADIUS, startY + RADIUS);
//        canvas.drawArc(rectF, 180, 180, false, bitmapPaint);

//        Path path = new Path();
//        path.moveTo(startX, startY);
//        path.lineTo(endX, startY);
//        path.lineTo(centerX, endY);
//        path.lineTo(startX, startY);
//        canvas.drawPath(path, bitmapPaint);

        if(drawTime && timeValue != null) {
            canvas.drawText(timeValue, centerX - paint.measureText(timeValue) / 2, startY + 2, paint);
        }

    }

    public boolean isInside(float x, float y) {
        return x >= (startX - DEFAULT_MARGIN) && x <= (endX + DEFAULT_MARGIN) && y >= (startY - DEFAULT_MARGIN) && y <= (endY + DEFAULT_MARGIN);
    }

    public int getRulerValue() {
        return rulerValue;
    }

    public void setRulerValue(int rulerValue) {
        this.rulerValue = rulerValue;
    }

    public String getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(String timeValue) {
        this.timeValue = timeValue;
    }

    public void setCircleColor(int color) {
        bitmapPaint.setColor(color);
    }
}
