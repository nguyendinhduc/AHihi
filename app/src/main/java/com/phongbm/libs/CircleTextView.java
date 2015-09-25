package com.phongbm.libs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class CircleTextView extends TextView {
    private int circleColor = Color.WHITE;
    private Paint paint = new Paint();

    public CircleTextView(Context context) {
        super(context);
    }

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        String circleColorString = attrs.getAttributeValue(null, "circleColor");
        if (circleColorString != null) {
            circleColor = Color.parseColor(circleColorString);
        }
    }

    public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewLength = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(viewLength, viewLength);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = getHeight() * 0.5f;
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(circleColor);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.reset();
        float textSize = getTextSize();
        paint.setTextSize(textSize);
        paint.setColor(getCurrentTextColor());
        String text = getText().toString();
        float textWidth = paint.measureText(text);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float x = radius - (textWidth * 0.5f);
        float y = radius - ((fontMetrics.ascent + fontMetrics.descent) * 0.5f);
        canvas.drawText(text, x, y, paint);
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
    }

}