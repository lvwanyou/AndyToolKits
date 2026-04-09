package com.example.openglexample.widget.imgView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class CanvasView extends View {
    private Paint paint; // 创建画笔

    // 构造函数
    public CanvasView(Context context) {
        this(context, null);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(); // 初始化画笔
    }

    // 初始化画笔的方法
    private void init() {
        paint = new Paint();
        paint.setColor(0xFF000000); // 设置画笔颜色为黑色
        paint.setStrokeWidth(5); // 设置画笔宽度
        paint.setAntiAlias(true); // 设置抗锯齿
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 在Canvas上画线，从(100, 100)到(200, 200)
        canvas.drawLine(100, 100, 200, 200, paint);
    }
}
