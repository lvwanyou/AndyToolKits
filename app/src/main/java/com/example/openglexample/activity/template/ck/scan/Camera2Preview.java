/*
package com.example.openglexample.activity.template.ck.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.dianping.video.util.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

public class Camera2Preview extends TextureView {
    private static final String TAG = "Camera2Preview";
    private final Context context;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private Size previewSize;
    private CaptureRequest.Builder previewRequestBuilder;

    private ImageReader imageReader;

    public Camera2Preview(Context context) {
        super(context);
        this.context = context;
        this.setSurfaceTextureListener(surfaceTextureListener);
    }

    private final SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private void openCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "openCamera: Cannot access the camera.", e);
        } catch (SecurityException e) {
            Log.e(TAG, "openCamera: Camera permission not granted.", e);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;

            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            closeCamera(camera);
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            closeCamera(camera);
        }
    };

    private void closeCamera(CameraDevice camera) {
        camera.close();
        cameraDevice = null;
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    @SuppressLint("WrongConstant")
    private void createCameraPreviewSession() {
        try {

            // 在初始化Camera2 API时
            imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(),
                    ImageFormat.YUV_420_888, 2 */
/* images buffered *//*
);
            imageReader.setOnImageAvailableListener(
                    reader -> {
                        try (Image image = reader.acquireNextImage()) {
                            // 处理图像数据
//                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                            byte[] data = new byte[buffer.remaining()];
//                            buffer.get(data);
//                            // 你可以在这里添加二维码解码逻辑，比如使用ZXing的PlanarYUVLuminanceSource
                            byte[] data = BitmapUtil.generateNV21Data(image);


                        }
                    }, null
            );

            SurfaceTexture texture = this.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(texture);
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(previewSurface);
            previewRequestBuilder.addTarget(imageReader.getSurface());

//            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            previewRequestBuilder.addTarget(imageReader.getSurface());

            List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(previewSurface); // 预览Surface
            outputSurfaces.add(imageReader.getSurface()); // ImageReader的Surface

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    captureSession = session;
                    try {
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        captureSession.setRepeatingRequest(previewRequest, null, null);
//                        captureSession.capture(previewRequest, null, null);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "createCameraPreviewSession: ", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(context, "Camera configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "createCameraPreviewSession: ", e);
        }
    }

}
*/
