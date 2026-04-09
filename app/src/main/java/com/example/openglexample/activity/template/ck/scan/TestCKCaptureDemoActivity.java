/*
package com.example.openglexample.activity.template.ck.scan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.dianping.video.PeacockCKSolution;
import com.dianping.video.model.CKCompileParam;
import com.dianping.video.model.CutSameModel;
import com.dianping.video.template.CKPlayerStateListenerAdapter;
import com.dianping.video.template.CutSameTemplateVideoPreview;
import com.dianping.video.template.model.material.core.VideoMaterial;
import com.dianping.video.utils.Const;
import com.dianping.video.utils.DataHelper;
import com.example.openglexample.R;
import com.example.openglexample.activity.template.ck.TestCKCutSameThumbnailActivity;
import com.example.openglexample.utils.CKUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class TestCKCaptureDemoActivity extends Activity {
    public static final String TAG = TestCKCaptureDemoActivity.class.getSimpleName();

    private static final int PICK_IMAGE_VIDEO_MULTI = 64;
    private static final int PICK_IMAGE_VIDEO_SINGLE = 65;

    CutSameTemplateVideoPreview templateVideoPreview;
    AppCompatSeekBar videoSeek;
    ImageView maskImgView;

    private CutSameModel cutSameModel;

    private boolean isSetDuration = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ck_capture_demo);

        PeacockCKSolution.getInstance().init(getApplicationContext(), Const.UGC_PRIVACY_TOKEN_NOTE, new PeacockCKSolution.CKInitListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onInitPreparing() {

            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {

            }
        });

        templateVideoPreview = findViewById(R.id.test_ck_surfaceview);

        // 解析扫码结果
        String result = getIntent().getStringExtra("scanResult");
        // 提取template查询参数
        String encodedJson = result.substring(result.indexOf("template=") + 9);
        if (!TextUtils.isEmpty(encodedJson)) {
            try {
                String decodedString = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8.toString());
                JSONObject jsonObject = new JSONObject(decodedString);
                JSONArray segmentDurationsArray = jsonObject.getJSONArray("segmentDurations");
                int segmentDurationsLength = segmentDurationsArray.length();

                this.cutSameModel = CKUtils.mockCreateCutSameModelWithoutMaterials(
                        jsonObject.optString("downloadUrl"),
                        jsonObject.optString("signature"),
                        jsonObject.optInt("volcengineTemplateType"),
                        jsonObject.optString("effectResourceUrl"),
                        segmentDurationsLength);

                this.judgeMaterialsPrepared();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        initTemplateVideoPreview();

        findViewById(R.id.test_ck_select_media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestCKCaptureDemoActivity.this.judgeMaterialsPrepared();
                // 选择素材验证
                Intent intent = new Intent();
                intent.setType("image/* video/*"); // 设置类型，选择图片和视频
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 允许多选
                intent.setAction(Intent.ACTION_GET_CONTENT); // 获取内容
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_VIDEO_MULTI);
            }
        });

        findViewById(R.id.test_ck_select_pic_media).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                TestCKCaptureDemoActivity.this.judgeMaterialsPrepared();
                // 选择素材验证
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/* video/*"); // 设置类型，选择图片和视频
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_VIDEO_SINGLE);
            }
        });

        findViewById(R.id.test_ck_prepare_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TestCKCaptureDemoActivity.this.judgeMaterialsPrepared()) {
                    // 为了保证 ck 模版 mock 的数据是有的，在此处预下载
                    PeacockCKSolution.CKProgressDealSourceListener a1 = new PeacockCKSolution.CKProgressDealSourceListener() {
                        @Override
                        public void onProgress(float progress) {
                        }

                        @Override
                        public void onSuccess() {
                            // 媒体素材处理
                            PeacockCKSolution.getInstance().compressTemplateSource(cutSameModel,
                                    new PeacockCKSolution.CKProgressDealSourceListener() {
                                        @Override
                                        public void onProgress(float progress) {
                                        }

                                        @Override
                                        public void onSuccess() {
                                            Log.e(TAG, "prepareTemplateSource-onSuccess: playVideo");
                                            templateVideoPreview.playVideo();
                                            templateVideoPreview.getVideoFrameWithTime(new int[]{300}, 1000, 1000, new PeacockCKSolution.CKGetImageListener() {
                                                @Override
                                                public void frameBitmap(String timeStamp, Bitmap bitmap) {
                                                    Bitmap a = bitmap;
                                                }

                                                @Override
                                                public void onError(int errorCode, String errorMsg) {

                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(int errorCode, @Nullable String errorMsg) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(int errorCode, @Nullable String errorMsg) {
                        }
                    };
                    PeacockCKSolution.getInstance().prepareTemplateSource(getApplicationContext(), cutSameModel, a1);
                }
            }
        });

        findViewById(R.id.test_ck_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TestCKCaptureDemoActivity.this.judgeMaterialsPrepared()) {
                    // 导出功能验证
                    String outputVideoRootPath = getApplicationContext().getExternalCacheDir().toString() + "/temp";
                    File outputVideoDir = new File(outputVideoRootPath);
                    boolean dirExistFlag = outputVideoDir.exists();
                    if (!dirExistFlag) {
                        dirExistFlag = outputVideoDir.mkdirs();
                    }
                    if (dirExistFlag) {
                        String outputVideoName = "temp" + new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(new Date()) + ".mp4";
                        String outputVideoPath = outputVideoRootPath + "/" + outputVideoName;
                        PeacockCKSolution.getInstance().exportSource(cutSameModel, outputVideoPath, createCompileParam(), new PeacockCKSolution.CKProgressDealSourceListener() {
                            @Override
                            public void onProgress(float progress) {

                            }

                            @Override
                            public void onSuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TestCKCaptureDemoActivity.this, "导出成功，导出路径为：" + outputVideoPath, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(int errorCode, @Nullable String errorMsg) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TestCKCaptureDemoActivity.this, "导出失败，导出路径为：" + outputVideoPath, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });

        findViewById(R.id.test_ck_get_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TestCKCaptureDemoActivity.this.judgeMaterialsPrepared()) {
                    // 抽帧功能验证
//            int[] timeStamps = {500, 2000, 5000};
//            PeacockCKSolution.getInstance().getVideoFrameWithTime(cutSameModel, timeStamps, 500, 500, null);

                    templateVideoPreview.release();
                    // 抽帧缩略图验证
                    Intent intent = new Intent(TestCKCaptureDemoActivity.this, TestCKCutSameThumbnailActivity.class);
                    intent.putExtra("usingExistVideoMaterials", true);
                    startActivity(intent);
                }
            }
        });

        videoSeek = findViewById(R.id.test_ck_seekbar);
        videoSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                templateVideoPreview.pauseVideo();
                templateVideoPreview.isMove(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        templateVideoPreview.isMove(false);
                    }
                }, 200);
                templateVideoPreview.seekTo(seekBar.getProgress());

                Log.i("xueleiwu", "onPlayerProgress():" + seekBar.getProgress());
            }
        });

        maskImgView = findViewById(R.id.test_ck_mask_click);
        maskImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (templateVideoPreview.isPlaying()) {
                    templateVideoPreview.pauseVideo();
                } else {
                    templateVideoPreview.playVideo();
                }
            }
        });
    }

    private void initTemplateVideoPreview() {
        templateVideoPreview.init(cutSameModel);
        templateVideoPreview.setCKPlayerStateListener(new CKPlayerStateListenerAdapter() {
            @Override
            public void onPlayerProgress(long progressTime, long progressDuration) {
                int progress = (int) ((progressTime * 100) / progressDuration);
                videoSeek.setProgress(progress);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        templateVideoPreview.registerListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        templateVideoPreview.unRegisterListener();
        templateVideoPreview.pauseVideo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            templateVideoPreview.release();
            PeacockCKSolution.getInstance().releaseAllTasks();
        } catch (Exception e) {
            Log.e(TAG, "onDestroy exception");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 如果是多选，data.getClipData()获取多个数据
            if (requestCode == PICK_IMAGE_VIDEO_MULTI) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        this.addVideoMaterial(item.getUri());
                    }

                    judgeMaterialsPrepared(true);
                }
            } else {
                // 如果是单选，data.getData()获取数据
                this.addVideoMaterial(data.getData());

                judgeMaterialsPrepared(true);
            }
        }
    }

    private void addVideoMaterial(Uri uri) {
        if (uri != null && this.cutSameModel != null && this.cutSameModel.getVideoMaterials() != null) {
            this.cutSameModel.getVideoMaterials().size();
            // 处理uri...
            boolean isPhoto = CKUtils.isImageUri(getApplicationContext(), uri);
            if (this.cutSameModel.getVideoMaterials() == null) {
                this.cutSameModel.setVideoMaterials(new ArrayList<>());
            }
            VideoMaterial videoMaterial = new VideoMaterial(String.valueOf(this.cutSameModel.getVideoMaterials().size() + 1));
            long oriDuration = isPhoto ? 0 : DataHelper.getVideoDuration(getApplicationContext(), uri.toString());

            videoMaterial.setPath(uri.toString(), isPhoto);
            videoMaterial.setSourceTimeRange(0, (int) oriDuration);
            this.cutSameModel.getVideoMaterials().add(videoMaterial);
        }
    }

    private boolean judgeMaterialsPrepared(boolean showSuccToast) {
        String toastTxt = "";
        int duration = Toast.LENGTH_SHORT;
        if (this.cutSameModel.getVideoMaterials() == null || this.cutSameModel.getVideoMaterials().size() == 0) {
            toastTxt = "请先选择 " + CKUtils.videoMaterialNum + "个素材";
        } else if (this.cutSameModel.getVideoMaterials().size() < CKUtils.videoMaterialNum) {
            toastTxt = "已选择 " + this.cutSameModel.getVideoMaterials().size() + " 个素材, 仍需选择 " + (CKUtils.videoMaterialNum - this.cutSameModel.getVideoMaterials().size()) + "个素材";

        } else if (this.cutSameModel.getVideoMaterials().size() > CKUtils.videoMaterialNum) {
            toastTxt = "已选择 " + this.cutSameModel.getVideoMaterials().size() + " 个素材, 默认使用最新选择的 " + CKUtils.videoMaterialNum + "个素材, 删除多余素材";
            duration = Toast.LENGTH_LONG;
            this.cutSameModel.setVideoMaterials(new ArrayList<>(this.cutSameModel.getVideoMaterials().subList(this.cutSameModel.getVideoMaterials().size() - CKUtils.videoMaterialNum, this.cutSameModel.getVideoMaterials().size())));
        } else if (this.cutSameModel.getVideoMaterials().size() == CKUtils.videoMaterialNum) {
            toastTxt = showSuccToast ? "素材选择完成✅" : "";
            CKUtils.videoMaterials = this.cutSameModel.getVideoMaterials();
        } else {
            toastTxt = "素材选择存在异常";
        }
        final String finalToastTxt = toastTxt;
        final int finalDuration = duration;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TestCKCaptureDemoActivity.this, finalToastTxt, finalDuration).show();
            }
        });
        return this.cutSameModel.getVideoMaterials() != null && this.cutSameModel.getVideoMaterials().size() >= CKUtils.videoMaterialNum;
    }

    private boolean judgeMaterialsPrepared() {
        return judgeMaterialsPrepared(false);
    }


    public CKCompileParam createCompileParam() {
        boolean supportHwEncoder = true;
        int bps = 16 * 1024 * 1024;
        int fps = 30;
        int gopSize = 35;
        return new CKCompileParam(CKCompileParam.CKExportResolution.V_1080P, supportHwEncoder, bps, fps, gopSize);
    }
}
*/
