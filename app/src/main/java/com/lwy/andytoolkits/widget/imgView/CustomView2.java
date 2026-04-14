package com.lwy.andytoolkits.widget.imgView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class CustomView2 extends View {
    private Paint mBackgroundPaint;
    private Paint mSpecialShapePaint;

    public CustomView2(Context context) {
        super(context);
        init();
    }

    public CustomView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化背景画笔
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.WHITE);

        // 初始化特殊形状画笔
        mSpecialShapePaint = new Paint();
        mSpecialShapePaint.setColor(Color.RED);
        mSpecialShapePaint.setStyle(Paint.Style.FILL);
        mSpecialShapePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制背景
        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);

        // 绘制特殊形状
        int rectWidth = getWidth() / 2;
        int rectHeight = getHeight() / 2;
        int cornerSize = 50;

        // 绘制矩形
        canvas.drawRect(rectWidth, rectHeight, getWidth() - rectWidth, getHeight() - rectHeight, mSpecialShapePaint);

        // 绘制四个边角
        mSpecialShapePaint.setColor(Color.RED);
        canvas.drawRect(rectWidth - cornerSize, rectHeight - cornerSize, rectWidth, rectHeight, mSpecialShapePaint);
        canvas.drawRect(getWidth() - rectWidth, rectHeight - cornerSize, getWidth() - rectWidth + cornerSize, rectHeight, mSpecialShapePaint);
        canvas.drawRect(rectWidth - cornerSize, getHeight() - rectHeight, rectWidth, getHeight() - rectHeight + cornerSize, mSpecialShapePaint);
        canvas.drawRect(getWidth() - rectWidth, getHeight() - rectHeight, getWidth() - rectWidth + cornerSize, getHeight() - rectHeight + cornerSize, mSpecialShapePaint);
    }
    // canvas 设置一个背景， 然后在背景上会绘制一个特殊的形状的框，特殊形状
}

