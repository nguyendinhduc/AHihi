package com.phongbm.libs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class TriangleShapeView extends View {
    private int color;
    private String direction;

    public TriangleShapeView(Context context) {
        super(context);
    }

    public TriangleShapeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TriangleShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        direction = attrs.getAttributeValue(null, "direction");
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth() / 2;
        Path path = new Path();
        path.moveTo(w, 0);
        switch (direction) {
            case "left":
                path.lineTo(2 * w, 0);
                path.lineTo(2 * w, w);
                path.lineTo(w, 0);
                break;
            case "right":
                path.lineTo(0, 0);
                path.lineTo(0, w);
                path.lineTo(w, 0);
                break;
        }
        path.close();
        Paint p = new Paint();
        p.setColor(color);
        canvas.drawPath(path, p);
    }

    public void setBackgroundColor(int color) {
        this.color = color;
        this.postInvalidate();
    }

}