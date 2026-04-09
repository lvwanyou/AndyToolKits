package com.example.openglexample.widget.imgView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class CustomView3 extends View {
    private Paint mBackgroundPaint;
    private Paint mSpecialShapePaint;

    public CustomView3(Context context) {
        super(context);
        init();
    }

    public CustomView3(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView3(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mSpecialShapePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    protected int mWidth, mHeight;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        // 绘制背景
//        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);
//
//        // 绘制特殊形状
//        int rectWidth = getWidth() / 2;
//        int rectHeight = getHeight() / 2;
//        int cornerSize = 50;
//
//        // 绘制矩形
//        canvas.drawRect(rectWidth, rectHeight, getWidth() - rectWidth, getHeight() - rectHeight, mSpecialShapePaint);
//
//        // 绘制四个边角
//        mSpecialShapePaint.setColor(Color.RED);
//        canvas.drawRect(rectWidth - cornerSize, rectHeight - cornerSize, rectWidth, rectHeight, mSpecialShapePaint);
//        canvas.drawRect(getWidth() - rectWidth, rectHeight - cornerSize, getWidth() - rectWidth + cornerSize, rectHeight, mSpecialShapePaint);
//        canvas.drawRect(rectWidth - cornerSize, getHeight() - rectHeight, rectWidth, getHeight() - rectHeight + cornerSize, mSpecialShapePaint);
//        canvas.drawRect(getWidth() - rectWidth, getHeight() - rectHeight, getWidth() - rectWidth + cornerSize, getHeight() - rectHeight + cornerSize, mSpecialShapePaint);


        int left = -mWidth;
        int top = -mHeight;
        int right = mWidth * 2;
        int bottom = mHeight * 2;
        int layerId = canvas.saveLayer(left, top, right, bottom, null, Canvas.ALL_SAVE_FLAG);
        canvas.drawRect(left, top, right, bottom, mBackgroundPaint); // 绘制背景矩形

        mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT)); // 设置混合模式
        mBackgroundPaint.setColor(Color.WHITE);

        // 创建Path对象并定义带有边角的矩形裁剪区域
        Path clipPath = new Path();
        // 假设cornerLength是边角线的长度
        float cornerLength = 50;
        // 定义矩形裁剪区域的四个角的坐标
        float startX = getWidth() / 2;
        float startY = getHeight() / 2;
        float endX = startX + 400;
        float endY = startY + 200;

// 绘制左上角的边角
        clipPath.moveTo(startX, startY + cornerLength);
        clipPath.lineTo(startX, startY);
        clipPath.lineTo(startX + cornerLength, startY);

// 绘制右上角的边角
        clipPath.moveTo(endX - cornerLength, startY);
        clipPath.lineTo(endX, startY);
        clipPath.lineTo(endX, startY + cornerLength);

// 绘制右下角的边角
        clipPath.moveTo(endX, endY - cornerLength);
        clipPath.lineTo(endX, endY);
        clipPath.lineTo(endX - cornerLength, endY);

// 绘制左下角的边角
        clipPath.moveTo(startX + cornerLength, endY);
        clipPath.lineTo(startX, endY);
        clipPath.lineTo(startX, endY - cornerLength);

// 连接四个角，形成一个完整的裁剪区域
        clipPath.close();

// 绘制带有边角的矩形裁剪区域
        canvas.drawPath(clipPath, mBackgroundPaint);
        canvas.drawRect(new Rect((int) (startX -cornerLength) , (int) (startY -cornerLength), (int) (endX - cornerLength), (int) (endY - cornerLength)), mBackgroundPaint);

        mBackgroundPaint.setXfermode(null); // 清除混合模式
        canvas.restoreToCount(layerId); // 恢复画布状态

    }
    // canvas 设置一个背景， 然后在背景上会绘制一个特殊的形状的框，特殊形状


}

