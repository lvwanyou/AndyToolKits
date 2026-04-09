package com.example.openglexample.widget.rulerView.v1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by lvwanyou
 * <p>
 * 水平进度滚轮视图
 */

public class HorizontalProgressWheelView extends View {

    private final Rect mCanvasClipBounds = new Rect();

    private ScrollingListener mScrollingListener;
    private float mLastTouchedPosition;

    private Paint mProgressSmallDialPaint, mProgressMediumDialPaint;
    private Paint mProgressMiddleLinePaint;
    private int mProgressDialWidth, mProgressMediumDialWidth, mProgressSmallDialHeight, mProgressMediumDialHeight, mMiddleLineHeight;
    private int mProgressDialMargin;

    private boolean mScrollStarted;
    private float mTotalScrollDistance;

    private int mMiddleLineColor;

    public HorizontalProgressWheelView(Context context) {
        this(context, null);
    }

    public HorizontalProgressWheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalProgressWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化方法，用于初始化视图和画笔
     */
    private void init() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        // 设置中间线的颜色
        mMiddleLineColor = Color.parseColor("#FFFFFFFF");

        // 设置刻度的宽度和高度
        mProgressDialWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, displayMetrics);
        mProgressMediumDialWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, displayMetrics);
        mProgressSmallDialHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, displayMetrics);
        mProgressMediumDialHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);
        mMiddleLineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, displayMetrics);
        mProgressDialMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, displayMetrics);

        // 初始化刻度画笔
        mProgressSmallDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressSmallDialPaint.setStyle(Paint.Style.STROKE);
        mProgressSmallDialPaint.setStrokeWidth(mProgressDialWidth);
        mProgressSmallDialPaint.setColor(Color.parseColor("#7AFFFFFF"));

        // 初始化中等刻度相关画笔
        // 初始化中等刻度画笔
        mProgressMediumDialPaint = new Paint(mProgressSmallDialPaint);
        mProgressMediumDialPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressMediumDialPaint.setStrokeWidth(mProgressMediumDialWidth);
        mProgressMediumDialPaint.setColor(Color.parseColor("#B3FFFFFF"));

        // 初始化中间线画笔
        mProgressMiddleLinePaint = new Paint(mProgressMediumDialPaint);
        mProgressMiddleLinePaint.setColor(Color.parseColor("#FFFFFFFF"));
    }

    /**
     * 绘制方法，用于绘制刻度和中间线
     *
     * @param canvas 画布对象
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        // 获取画布的裁剪边界
        canvas.getClipBounds(mCanvasClipBounds);
        // 绘制刻度, 整个大刻度盘范围在-45°—45°, 每个小刻度代表3°。
        int linesCount = 61;
        float deltaX = (mTotalScrollDistance) % (float) (mProgressDialMargin + mProgressDialWidth);

        for (int i = 0; i < linesCount; i++) {
            if (i % 10 == 0) {
                // 绘制中等刻度
                canvas.drawLine(
                        -deltaX + mCanvasClipBounds.left + i * (mProgressMediumDialWidth + mProgressDialMargin),
                        mCanvasClipBounds.centerY() - mProgressMediumDialHeight / 2.0f,
                        -deltaX + mCanvasClipBounds.left + i * (mProgressMediumDialWidth + mProgressDialMargin),
                        mCanvasClipBounds.centerY() + mProgressMediumDialHeight / 2.0f, mProgressMediumDialPaint);
            } else {
                // 绘制小刻度
                canvas.drawLine(
                        -deltaX + mCanvasClipBounds.left + i * (mProgressMediumDialWidth + mProgressDialMargin),
                        mCanvasClipBounds.centerY() - mProgressSmallDialHeight / 2.0f,
                        -deltaX + mCanvasClipBounds.left + i * (mProgressMediumDialWidth + mProgressDialMargin),
                        mCanvasClipBounds.centerY() + mProgressSmallDialHeight / 2.0f, mProgressSmallDialPaint);
            }
        }

        // 绘制中间线
        canvas.drawLine(
                mCanvasClipBounds.centerX(),
                mCanvasClipBounds.centerY() - mMiddleLineHeight / 2.0f,
                mCanvasClipBounds.centerX(),
                mCanvasClipBounds.centerY() + mMiddleLineHeight / 2.0f,
                mProgressMiddleLinePaint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchedPosition = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (mScrollingListener != null) {
                    mScrollStarted = false;
                    mScrollingListener.onScrollEnd();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - mLastTouchedPosition;
                if (distance != 0) {
                    if (!mScrollStarted) {
                        mScrollStarted = true;
                        if (mScrollingListener != null) {
                            mScrollingListener.onScrollStart();
                        }
                    }
                    onScrollEvent(event, distance);
                }
                break;
        }
        return true;
    }


    private void onScrollEvent(MotionEvent event, float distance) {
        mTotalScrollDistance -= distance;
        postInvalidate();
        mLastTouchedPosition = event.getX();
        if (mScrollingListener != null) {
            mScrollingListener.onScroll(-distance, mTotalScrollDistance);
        }
    }

    public void setScrollingListener(ScrollingListener scrollingListener) {
        mScrollingListener = scrollingListener;
    }

    public void setMiddleLineColor(@ColorInt int middleLineColor) {
        mMiddleLineColor = middleLineColor;
        mProgressMiddleLinePaint.setColor(mMiddleLineColor);
        invalidate();
    }

    public interface ScrollingListener {

        void onScrollStart();

        void onScroll(float delta, float totalDistance);

        void onScrollEnd();
    }
}
