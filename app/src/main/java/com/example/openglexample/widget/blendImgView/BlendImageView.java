package com.example.openglexample.widget.blendImgView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import com.example.openglexample.R;

public class BlendImageView extends android.support.v7.widget.AppCompatImageView {
    private Bitmap image1;
    private Bitmap image2;
    private Paint paint;

    public BlendImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlendImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        // 加载两张图片
        image1 = BitmapFactory.decodeResource(getResources(), R.drawable.aa);
        image2 = BitmapFactory.decodeResource(getResources(), R.drawable.bb);

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (image1 == null || image2 == null) {
            return;
        }

        // 创建一个新的 Bitmap 来绘制融合效果
        Bitmap result = Bitmap.createBitmap(image1.getWidth(), image1.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(result);

        // 绘制第一张图片
        resultCanvas.drawBitmap(image1, 0, 0, paint);

        // 设置混合模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));

        // 绘制第二张图片
        resultCanvas.drawBitmap(image2, 0, 0, paint);

        // 清除混合模式
        paint.setXfermode(null);

        // 将结果绘制到 ImageView 上
        canvas.drawBitmap(result, 0, 0, paint);
    }
}
