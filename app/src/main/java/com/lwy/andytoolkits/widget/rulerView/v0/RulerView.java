package com.lwy.andytoolkits.widget.rulerView.v0;

import android.annotation.SuppressLint;

import android.content.Context;

import android.graphics.Canvas;

import android.graphics.Color;

import android.graphics.Paint;

import android.support.annotation.Nullable;
import android.text.Layout;

import android.text.TextPaint;

import android.util.AttributeSet;

import android.util.Log;
import android.view.MotionEvent;

import android.view.VelocityTracker;

import android.view.View;

import android.view.ViewConfiguration;

import android.widget.Scroller;

/**
 * 卷尺控件类。由于时间比较紧，只有下班后有时间，因此只实现了基本功能。
 * <p>
 * 细节问题包括滑动过程中widget边缘的刻度显示问题等
 *
 * @version create：2014年8月26日
 */

@SuppressLint("ClickableViewAccessibility")
public class RulerView extends View {

    public interface OnValueChangeListener {

        public void onValueChange(float value);

    }

    public static final int MOD_TYPE_HALF = 2;

    public static final int MOD_TYPE_ONE = 10;

    private static final int ITEM_HALF_DIVIDER = 10;

    private static final int ITEM_ONE_DIVIDER = 10;

    private static final int ITEM_MAX_HEIGHT = 20;

    private static final int ITEM_MIN_HEIGHT = 10;

    private static final int TEXT_SIZE = 7;

    private float mDensity;

    private int mValue = 25, mMaxValue = 50, mModType = MOD_TYPE_ONE,

    mLineDivider = ITEM_HALF_DIVIDER;

// private int mValue = 50, mMaxValue = 500, mModType = MOD_TYPE_ONE,

// mLineDivider = ITEM_ONE_DIVIDER;

    /**
     * 表示手势滑动举例
     */
    private int mLastX, mTouchMove;

    private int mWidth, mHeight;

    private int mMinVelocity;

    private Scroller mScroller;

    private VelocityTracker mVelocityTracker;

    private OnValueChangeListener mListener;

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(getContext());
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mMinVelocity = ViewConfiguration.get(getContext())
                .getScaledMinimumFlingVelocity();
    }

    /**
     * 考虑可扩展，但是时间紧迫，只可以支持两种类型效果图中两种类型
     */
    public void initViewParam(int defaultValue, int maxValue, int model) {

        switch (model) {

            case MOD_TYPE_HALF:

                mModType = MOD_TYPE_HALF;

                mLineDivider = ITEM_HALF_DIVIDER;

                mValue = defaultValue * 2;

                mMaxValue = maxValue * 2;

                break;

            case MOD_TYPE_ONE:

                mModType = MOD_TYPE_ONE;

                mLineDivider = ITEM_ONE_DIVIDER;

                mValue = defaultValue;

                mMaxValue = maxValue;

                break;

            default:

                break;

        }

        invalidate();

        mLastX = 0;

        mTouchMove = 0;

        notifyValueChange();

    }

    /**
     * 设置用于接收结果的监听器
     *
     * @param listener
     */

    public void setValueChangeListener(OnValueChangeListener listener) {

        mListener = listener;

    }

    /**
     * 获取当前刻度值
     *
     * @return
     */

    public float getValue() {

        return mValue;

    }

    public void setValue(int value) {

        mValue = value;

        notifyValueChange();

        postInvalidate();

    }

    public void setValueToChange(int what) {

        mValue += what;

        notifyValueChange();

        postInvalidate();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = getWidth();
        mHeight = getHeight();
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScaleLine(canvas);
// drawWheel(canvas);
        drawMiddleLine(canvas);
    }

    /**
     * 从中间往两边开始画刻度线
     *
     * @param canvas
     */
    private void drawScaleLine(Canvas canvas) {

        canvas.save();

        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.rgb(141, 189, 225));
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.rgb(68, 135, 188));
        textPaint.setTextSize(TEXT_SIZE * mDensity);
        int width = mWidth, drawCount = 0;
        float xPosition = 0, textWidth = Layout.getDesiredWidth("0", textPaint);
        for (int i = 0; drawCount <= 4 * width; i++) {
            int numSize = String.valueOf(mValue + i).length();
// 中间线前面的刻度线
            xPosition = (width / 2 - mTouchMove) + i * mLineDivider * mDensity;
            if (xPosition + getPaddingRight() < mWidth) {
                if ((mValue + i) % mModType == 0) {
                    linePaint.setColor(Color.rgb(68, 135, 188));
                    if (mValue + i <= mMaxValue) {
                        canvas.drawLine(xPosition, getPaddingTop(), xPosition,
                                mDensity * ITEM_MAX_HEIGHT + getPaddingTop(), linePaint);
                        switch (mModType) {
                            case MOD_TYPE_HALF:
                                canvas.drawText(
                                        String.valueOf((mValue + i) / 2),
                                        countLeftStart(mValue + i, xPosition,
                                                textWidth),
                                        getHeight() - textWidth, textPaint);
                                break;
                            case MOD_TYPE_ONE:
                                canvas.drawText(String.valueOf(mValue + i),
                                        xPosition - (textWidth * numSize / 2),
                                        getHeight() - textWidth, textPaint);
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    if (mValue + i <= mMaxValue) {
                        linePaint.setColor(Color.rgb(141, 189, 225));
// linePaint.setColor(Color.rgb(68, 135, 188));
                        canvas.drawLine(xPosition, getPaddingTop(), xPosition,
                                mDensity * ITEM_MIN_HEIGHT + getPaddingTop(), linePaint);
                    }
                }
            }

// 中间线后面的刻度线
            xPosition = (width / 2 - mTouchMove) - i * mLineDivider * mDensity;
            if (xPosition > getPaddingLeft()) {
                if ((mValue - i) % mModType == 0) {
                    if (mValue - i >= 0) {
                        linePaint.setColor(Color.rgb(68, 135, 188));
                        canvas.drawLine(xPosition, getPaddingTop(), xPosition,
                                mDensity * ITEM_MAX_HEIGHT + getPaddingTop(), linePaint);
                        switch (mModType) {
                            case MOD_TYPE_HALF:
                                canvas.drawText(
                                        String.valueOf((mValue - i) / 2),
                                        countLeftStart(mValue - i, xPosition, textWidth),
                                        getHeight() - textWidth, textPaint);
                                break;
                            case MOD_TYPE_ONE:
                                canvas.drawText(String.valueOf(mValue - i),
                                        xPosition - (textWidth * numSize / 2),
                                        getHeight() - textWidth, textPaint);
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    if (mValue - i >= 0) {
                        linePaint.setColor(Color.rgb(141, 189, 225));
                        canvas.drawLine(xPosition, getPaddingTop(), xPosition,
                                mDensity * ITEM_MIN_HEIGHT + getPaddingTop(), linePaint);
                    }
                }
            }
            drawCount += 2 * mLineDivider * mDensity;
        }
        canvas.restore();
    }

    /**
     * 计算没有数字显示位置的辅助方法
     *
     * @param value
     * @param xPosition
     * @param textWidth
     * @return
     */
    private float countLeftStart(int value, float xPosition, float textWidth) {
        float xp = 0f;
        if (value < 20) {
            xp = xPosition - (textWidth * 1 / 2);
        } else {
            xp = xPosition - (textWidth * 2 / 2);
        }
        return xp;
    }

    /**
     * 画中间的红色指示线、阴影等。指示线两端简单的用了两个矩形代替
     *
     * @param canvas
     */

    private void drawMiddleLine(Canvas canvas) {
        // TODO 常量太多，暂时放这，最终会放在类的开始，放远了怕很快忘记
        int gap = 12, indexWidth = 2, indexTitleWidth = 24, indexTitleHight = 10, shadow = 6;
        String color = "#66999999";
        canvas.save();
        Paint redPaint = new Paint();
        redPaint.setStrokeWidth(indexWidth);
        redPaint.setColor(Color.RED);
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, mHeight, redPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int xPosition = (int) event.getX();
        Log.e("----****111", "onTouchEvent1111: " + action);
        Log.e("----****", "onTouchEvent: " + xPosition);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mLastX = xPosition;
                mTouchMove = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                mTouchMove += (mLastX - xPosition);
                changeMoveAndValue();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                countMoveEnd();
                countVelocityTracker(event);
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
// break;
            default:
                break;
        }
        mLastX = xPosition;
        return true;
    }

    private void countVelocityTracker(MotionEvent event) {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > mMinVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE,
                    Integer.MAX_VALUE, 0, 0);
        }
    }

    private void changeMoveAndValue() {
        int tValue = (int) (mTouchMove / (mLineDivider * mDensity));
        int lastValue = mValue;
        if (Math.abs(tValue) > 0) {
            mValue += tValue;
            mTouchMove -= tValue * mLineDivider * mDensity;
            if (mValue <= 0 || mValue > mMaxValue) {
                mValue = mValue <= 0 ? 0 : mMaxValue;
                mTouchMove = 0;
                mScroller.forceFinished(true);
            }
            notifyValueChange();
        }
        if (lastValue != mValue) {
            postInvalidate();
        }
    }

    private void countMoveEnd() {
        int roundMove = Math.round(mTouchMove / (mLineDivider * mDensity));
        int lastValue = mValue;
        mValue = mValue + roundMove;
        mValue = mValue <= 0 ? 0 : mValue;
        mValue = mValue > mMaxValue ? mMaxValue : mValue;
        mLastX = 0;
        mTouchMove = 0;
        notifyValueChange();
        if (lastValue != mValue) {
            postInvalidate();
        }
    }

    private void notifyValueChange() {
        if (null != mListener) {
            if (mModType == MOD_TYPE_ONE) {
                mListener.onValueChange(mValue);
            }
            if (mModType == MOD_TYPE_HALF) {
                mListener.onValueChange(mValue / 2f);
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) { // over
                countMoveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mTouchMove += (mLastX - xPosition);
                changeMoveAndValue();
                mLastX = xPosition;
            }
        }
    }

    @Override

    public boolean dispatchTouchEvent(MotionEvent event) {

        getParent().requestDisallowInterceptTouchEvent(true);

        return super.dispatchTouchEvent(event);

    }

}