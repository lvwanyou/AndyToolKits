package com.example.openglexample.widget.blendImgView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.example.openglexample.R;

public class BlendImageView1 extends android.support.v7.widget.AppCompatImageView {
    private Bitmap image1;
    private Bitmap image2;
    private Paint paint;

    public BlendImageView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlendImageView1(Context context) {
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

        // 计算图片的绘制位置
        int width = getWidth();
        int height = getHeight();
        int imageHeight = height / 2;

        // 创建一个新的 Bitmap 来绘制融合效果
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(result);

        // 绘制第一张图片
        resultCanvas.drawBitmap(image1, 0, 0, paint);

        // 创建渐变效果
        LinearGradient gradient = new LinearGradient(0, imageHeight - (image2.getHeight() / 2), 0, imageHeight + (image2.getHeight() / 2),
                0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
        paint.setShader(gradient);

        // 设置混合模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        // 绘制渐变效果
        resultCanvas.drawRect(0, imageHeight - (image2.getHeight() / 2), width, imageHeight + (image2.getHeight() / 2), paint);

        // 清除混合模式
        paint.setXfermode(null);
        paint.setShader(null);

        // 绘制第二张图片
        resultCanvas.drawBitmap(image2, 0, imageHeight - (image2.getHeight() / 2), paint);

        // 将结果绘制到 ImageView 上
        canvas.drawBitmap(result, 0, 0, paint);
    }
}
