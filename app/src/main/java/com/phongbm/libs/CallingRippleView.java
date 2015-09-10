package com.phongbm.libs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.phongbm.ahihi.R;

import java.util.Timer;
import java.util.TimerTask;

public class CallingRippleView extends View {
    public static final int SPEED_FAST = 3;
    public static final int SPEED_SLOW = 1;
    public static final int BLACK = 0xFFFFFFFF;
    public static final int COLOR = 0xFF4CAF50;
    public static final int ALPHA = 0xFF000000;
    public static final int PAINTER_COUNT = 10;
    public static final int LINE_COUNT = 2;

    Paint[] painter;
    int[] currRadius;
    int minRadius, maxRadius;
    Timer animTimer;
    int currSpeed;
    int colorBase;
    int x, y;
    boolean inited;

    public CallingRippleView(Context context) {
        super(context);
        init();
    }

    public CallingRippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CallingRippleView(Context context, AttributeSet attrs,
                             int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inited = false;

        animTimer = new Timer();
        animTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (x == 0 || y == 0) {
                    x = getWidth() / 2;
                    y = getHeight() / 2;
                    return;
                } else if (!inited) {
                    initValues();
                    inited = true;
                }
                for (int i = 0; i < currRadius.length; ++i)
                    if (currRadius[i] > maxRadius) {
                        currRadius[i] = minRadius;
                    } else {
                        currRadius[i] += currSpeed;
                    }
                postInvalidate();
            }
        }, 0, 20);
    }

    private void initValues() {
        x = getWidth() / 2;
        y = getHeight() / 2;
        colorBase = COLOR / PAINTER_COUNT;
        initializePainter();
        currSpeed = SPEED_FAST;
        minRadius = (int) (getResources().getDimensionPixelSize(R.dimen.chat_panel_avatar_size) / 2);
        maxRadius = getWidth() < getHeight() ? getWidth() / 2 : getHeight() / 2;
        currRadius = new int[LINE_COUNT];
        for (int i = 0; i < currRadius.length; ++i) {
            currRadius[i] = minRadius + (maxRadius - minRadius) * i / currRadius.length;
        }
    }

    private void initializePainter() {
        painter = new Paint[PAINTER_COUNT];
        for (int i = 0; i < painter.length; ++i) {
            painter[i] = new Paint();
            painter[i].setColor((BLACK - colorBase * (PAINTER_COUNT - i)) | ALPHA);
            painter[i].setAntiAlias(true);
            painter[i].setStrokeWidth(PAINTER_COUNT - i);
            painter[i].setStyle(Style.STROKE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (inited)
            for (int i = 0; i < currRadius.length; ++i)
                canvas.drawCircle(x, y, currRadius[i], getPaint(currRadius[i]));
    }

    private Paint getPaint(int radius) {
        int divide = ((maxRadius - minRadius) / PAINTER_COUNT) + 1;
        int iter = (radius - minRadius) / divide;
        return painter[iter];
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animTimer != null)
            animTimer.cancel();
        super.onDetachedFromWindow();
    }

}