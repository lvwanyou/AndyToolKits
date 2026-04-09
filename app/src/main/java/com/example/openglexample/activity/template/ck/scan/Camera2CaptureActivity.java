/*
package com.example.openglexample.activity.template.ck.scan;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;


import com.dianping.util.PermissionCheckHelper;
import com.example.openglexample.R;

public class Camera2CaptureActivity extends Activity {
    private Camera2Preview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_scan_camera2);

        String[] permissionArray = {Manifest.permission.CAMERA};
        String[] messageArray = {"使用相机前应用需要获取您的相机的使用权限"};
        if (!PermissionCheckHelper.isPermissionGranted(this, Manifest.permission.CAMERA) || !PermissionCheckHelper.isPermissionGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE) || !PermissionCheckHelper.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || !PermissionCheckHelper.isPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
            PermissionCheckHelper.instance().requestPermissions(this, 0, permissionArray, messageArray, (requestCode, permissions, grantResults) -> {
                boolean isAllGranted = true;
                for (int result : grantResults) {
                    if (result != 0) {
                        isAllGranted = false;
                        break;
                    }
                }

                if (requestCode == 0 && isAllGranted) {
                    initWithPermission();
                } else {
                    finish();
                }
            });
        } else {
            initWithPermission();
        }
    }

    private void initWithPermission() {
        mPreview = new Camera2Preview(this);
        FrameLayout previewFrame = findViewById(R.id.camera2_preview);
        previewFrame.addView(mPreview);
    }

    // 在此处添加处理相机帧数据的代码，以及启动解码二维码的代码
//    private void processCameraFrameData() {
//        mPreview.
//
//        mPreview.setOnFrameAvailableListener((data, width, height) -> {
//            // Process frame data
//            // ...
//
//            // Start decoding QR code
//            // ...
//        });
//    }
}
*/
