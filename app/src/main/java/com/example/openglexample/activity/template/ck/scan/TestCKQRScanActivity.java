/*
package com.example.openglexample.activity.template.ck.scan;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dianping.video.util.ViewUtils;
import com.example.openglexample.R;
import com.example.openglexample.utils.CKUtils;
import com.meituan.android.edfu.mbar.camera.decode.MBarResult;
import com.meituan.android.edfu.mbar.util.BarcodeFormat;
import com.meituan.android.edfu.mbar.util.ImageScanCodeUtil;
import com.meituan.android.edfu.mbar.util.MbarConfig;
import com.meituan.android.edfu.mbar.util.Result;
import com.meituan.android.edfu.mbar.view.QRScanActivity;

public class TestCKQRScanActivity extends QRScanActivity {
    public static final String TAG = "TestCKQRScanActivity";
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;

    public TestCKQRScanActivity() {
    }

    public void onCreate(Bundle icicle) {
        this.setConfig((new MbarConfig.Builder()).appID(1).setPrivacyToken("dp-ae8525272f131c5c").autoZoomerTrigger(true).openMultiCodeScanner(true).setMaxNumber(5).ScanROI(new RectF(0.0F, 0.0F, 1.0F, 1.0F)).build());
        super.onCreate(icicle);

        CKUtils.checkBuildGradleSetting(this);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(-2147483648);
            window.setStatusBarColor(-1);
            if (Build.VERSION.SDK_INT >= 23) {
                window.getDecorView().setSystemUiVisibility(8192);
            }
        }
        this.getWindow().setFlags(1024, 1024);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void initMaskView(View view) {
        this.createCaptureView(view);
    }

    protected int getMaskViewId() {
        return R.layout.act_scan_edfu;
    }

    protected boolean checkCameraPermission() {
        return super.checkCameraPermission();
    }

    protected void createCaptureView(View view) {
        this.initTitleBar();
    }

    protected void initTitleBar() {
        ImageView leftView = (ImageView) this.findViewById(R.id.title_bar_btn);
        leftView.setOnClickListener(v -> TestCKQRScanActivity.this.onBackPressed());

        TextView albumView = findViewById(R.id.albumbutton);
        albumView.setOnClickListener(v -> chooseImageFromAlbum());
    }

    private void chooseImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
            String imagePath;
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                imagePath = cursor.getString(columnIndex);
                cursor.close();
            } else {
                imagePath = CKUtils.getPathFromUri(this, selectedImageUri);
            }

            if (!TextUtils.isEmpty(imagePath)) {
                ImageScanCodeUtil.scanCode(imagePath, (ImageScanCodeUtil.ImageScanCodeResultListener) result -> handleDecode(result));
            }
        }
    }

    public void handleDecode(final Result rawResult) {
        if (rawResult == null || rawResult.isMultiCodeScan && rawResult.multiCodeResult == null) {
            Log.e(TAG, "no scan result");
            this.handleFail();
        } else {
            if (rawResult.isMultiCodeScan) {
                RelativeLayout qrcodeSelectView = (RelativeLayout) this.findViewById(R.id.qrcode_select_view);
                if (rawResult.multiCodeResult.size() == 1) {
                    this.handleResult(((MBarResult) rawResult.multiCodeResult.get(0)).result, rawResult.getBarcodeFormat(), (int) ((MBarResult) rawResult.multiCodeResult.get(0)).type);
                    return;
                }

                for (int i = 0; i < rawResult.multiCodeResult.size(); ++i) {
                    final MBarResult result = (MBarResult) rawResult.multiCodeResult.get(i);
                    if (result == null) {
                        break;
                    }

                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewUtils.dip2px(this, 36.0F), ViewUtils.dip2px(this, 36.0F)));
                    float imgW = (float) rawResult.imgHeight;
                    float imgH = (float) rawResult.imgWidth;
                    float canvasW = (float) ViewUtils.getScreenWidthPixels(this);
                    float canvasH = (float) ViewUtils.getScreenHeightPixels(this);
                    float centerX = 1.0F - (result.y0 + result.y1) / 2.0F / (float) rawResult.imgHeight;
                    float centerY = (result.x0 + result.x1) / 2.0F / (float) rawResult.imgWidth;
                    float s_ho = 1.0F;
                    float s_ve = 1.0F;
                    if (canvasW / canvasH < imgW / imgH) {
                        s_ho = imgW / imgH * (canvasH / canvasW);
                    } else {
                        s_ve = imgH / imgW * (canvasW / canvasH);
                    }

                    centerX = 0.5F + s_ho * (centerX - 0.5F);
                    centerY = 0.5F + s_ve * (centerY - 0.5F);
                    centerX *= canvasW;
                    centerY *= canvasH;
                    centerX -= (float) ViewUtils.dip2px(this, 36.0F) / 2.0F;
                    centerY -= (float) ViewUtils.dip2px(this, 36.0F) / 2.0F;
                    imageView.setX(centerX);
                    imageView.setY(centerY);
                    Log.e("mbarscan", "rawResult.imgWidth=" + centerX + " ，rawResult.height=" + centerY + "，left=" + result.x0 + ", " + result.y0 + ",right=" + result.x1 + ", " + result.y1);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            TestCKQRScanActivity.this.handleResult(result.result, rawResult.getBarcodeFormat(), (int) result.type);
                        }
                    });
                    qrcodeSelectView.addView(imageView);
                }
            } else {
                this.handleResult(rawResult.getText(), rawResult.getBarcodeFormat(), rawResult.getCodeformat());
            }

        }
    }

    private void handleFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TestCKQRScanActivity.this, "Failed to decode QR code", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleResult(String result, BarcodeFormat resultFormat, int codeFormat) {
        if (TextUtils.isEmpty(result.trim())) {
            this.handleFail();
        } else {
            this.sendScanResult(result);
        }
    }

    private void sendScanResult(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TestCKQRScanActivity.this, "Success to decode QR code", Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(this, TestCKCaptureDemoActivity.class);
        intent.putExtra("scanResult", result);
        startActivity(intent);
        this.finish();
    }
}
*/
