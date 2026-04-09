package com.example.openglexample.widget.imgView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomView extends View {
    private Paint mBackgroundPaint;
    private Paint mSpecialShapePaint;

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化背景画笔
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.BLACK);

        // 初始化特殊形状画笔
        mSpecialShapePaint = new Paint();
        mSpecialShapePaint.setColor(Color.RED);
        mSpecialShapePaint.setStyle(Paint.Style.FILL);

//        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制背景
        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);

        // 绘制特殊形状
        int rectWidth = getWidth() / 4;
        int rectHeight = getHeight() / 4;
        int cornerSize = 50;

        // 绘制矩形
        canvas.drawRect(rectWidth, rectHeight, getWidth() - rectWidth, getHeight() - rectHeight, mSpecialShapePaint);

        // 绘制四个边角
        mSpecialShapePaint.setColor(Color.WHITE);
//        canvas.drawRect(rectWidth - cornerSize, rectHeight - cornerSize, rectWidth, rectHeight, mSpecialShapePaint);
//        canvas.drawRect(getWidth() - rectWidth, rectHeight - cornerSize, getWidth() - rectWidth + cornerSize, rectHeight, mSpecialShapePaint);
//        canvas.drawRect(rectWidth - cornerSize, getHeight() - rectHeight, rectWidth, getHeight() - rectHeight + cornerSize, mSpecialShapePaint);
//        canvas.drawRect(getWidth() - rectWidth, getHeight() - rectHeight, getWidth() - rectWidth + cornerSize, getHeight() - rectHeight + cornerSize, mSpecialShapePaint);

        // 左上角
        canvas.drawLine(rectWidth - cornerSize, rectHeight, rectWidth, rectHeight, mSpecialShapePaint);
        canvas.drawLine(rectWidth, rectHeight - cornerSize, rectWidth, rectHeight, mSpecialShapePaint);
        // 右上角
        canvas.drawLine(getWidth() - rectWidth, rectHeight - cornerSize, getWidth() - rectWidth, rectHeight, mSpecialShapePaint);
        canvas.drawLine(getWidth() - rectWidth, rectHeight, getWidth() - rectWidth + cornerSize, rectHeight, mSpecialShapePaint);
        // 左下角
        canvas.drawLine(rectWidth - cornerSize, getHeight() - rectHeight, rectWidth, getHeight() - rectHeight, mSpecialShapePaint);
        canvas.drawLine(rectWidth, getHeight() - rectHeight, rectWidth, getHeight() - rectHeight + cornerSize, mSpecialShapePaint);
        // 右下角
        canvas.drawLine(getWidth() - rectWidth, getHeight() - rectHeight, getWidth() - rectWidth + cornerSize, getHeight() - rectHeight, mSpecialShapePaint);
        canvas.drawLine(getWidth() - rectWidth, getHeight() - rectHeight, getWidth() - rectWidth, getHeight() - rectHeight + cornerSize, mSpecialShapePaint);
    }
}


