package com.phongbm.libs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class TriangleBackground extends View {

    public TriangleBackground(Context context) {
        super(context);
    }

    public TriangleBackground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TriangleBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        Path path = new Path();

        path.moveTo(0, 0);
        path.lineTo(w, 0);
        path.lineTo(w, h);
        path.lineTo(0, 0);

        path.close();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#2196f3"));
        canvas.drawPath(path, paint);
    }

}