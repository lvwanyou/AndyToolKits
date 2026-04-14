/*
package com.example.openglexample.activity.template.ck;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.dianping.video.PeacockCKSolution;
import com.dianping.video.model.CKCompileParam;
import com.dianping.video.model.CutSameModel;
import com.dianping.video.template.CKPlayerStateListenerAdapter;
import com.dianping.video.template.CutSameTemplateVideoPreview;
import com.dianping.video.utils.Const;
import com.example.openglexample.R;
import com.example.openglexample.utils.CKUtils;
import com.ss.android.ugc.cut_ui.ItemCrop;
import com.ss.android.ugc.cut_ui.MediaItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TestCKCutSamePreviewActivity extends Activity {
    public static final String TAG = TestCKCutSamePreviewActivity.class.getSimpleName();
    CutSameTemplateVideoPreview templateVideoPreview;
    AppCompatSeekBar videoSeek;
    ImageView maskImgView;
    Button nextBtn;
    Button updateTextBtn;
    Button updateMediaBtn;

    private CutSameModel cutSameModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ck_preview);

        CKUtils.checkBuildGradleSetting(this);

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



//********************* Mock 数据 begin *********************

        this.cutSameModel = CKUtils.mockCreateCutSameModel(CKUtils.templateUrl1, CKUtils.md51, CKUtils.templateType1, CKUtils.effectJsonUrl1, CKUtils.videoMaterialNum, this.getApplicationContext());


//********************* Mock 数据 end *********************



        initTemplateVideoPreview();

        findViewById(R.id.test_ck_template1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (templateVideoPreview.isPlaying()) {
                    templateVideoPreview.pauseVideo();
                }
                templateVideoPreview.release();
                CKUtils.videoMaterialNum = 7;
                cutSameModel = CKUtils.mockCreateCutSameModel(CKUtils.templateUrl1, CKUtils.md51, CKUtils.templateType1, CKUtils.effectJsonUrl1, CKUtils.videoMaterialNum, TestCKCutSamePreviewActivity.this.getApplicationContext());
                initTemplateVideoPreview();
            }
        });

        findViewById(R.id.test_ck_template2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (templateVideoPreview.isPlaying()) {
                    templateVideoPreview.pauseVideo();
                }
                templateVideoPreview.release();
                CKUtils.videoMaterialNum = 10;
                cutSameModel = CKUtils.mockCreateCutSameModel(CKUtils.templateUrl2, CKUtils.md52, CKUtils.templateType2, CKUtils.effectJsonUrl2, CKUtils.videoMaterialNum, TestCKCutSamePreviewActivity.this.getApplicationContext());
                initTemplateVideoPreview();
            }
        });

        findViewById(R.id.test_ck_template3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (templateVideoPreview.isPlaying()) {
                    templateVideoPreview.pauseVideo();
                }
                templateVideoPreview.release();
                CKUtils.videoMaterialNum = 8;
                cutSameModel = CKUtils.mockCreateCutSameModel(CKUtils.templateUrl3, CKUtils.md53, CKUtils.templateType3, CKUtils.effectJsonUrl3, CKUtils.videoMaterialNum, TestCKCutSamePreviewActivity.this.getApplicationContext());
                initTemplateVideoPreview();
            }
        });

        findViewById(R.id.test_ck_prepare_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        findViewById(R.id.test_ck_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 导出功能验证
                // 导出功能验证
                String outputVideoRootPath = getApplicationContext().getExternalCacheDir().toString() + "/temp";
                File outputVideoDir = new File(outputVideoRootPath);
                boolean dirExistFlag = false;
                if (!outputVideoDir.exists()) {
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
                                    Toast.makeText(TestCKCutSamePreviewActivity.this, "导出成功，导出路径为：" + outputVideoPath, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onError(int errorCode, @Nullable String errorMsg) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TestCKCutSamePreviewActivity.this, "导出失败，导出路径为：" + outputVideoPath, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }
        });

        findViewById(R.id.test_ck_get_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 抽帧功能验证
//            int[] timeStamps = {500, 2000, 5000};
//            PeacockCKSolution.getInstance().getVideoFrameWithTime(cutSameModel, timeStamps, 500, 500, null);

                templateVideoPreview.release();
                // 抽帧缩略图验证
                Intent intent = new Intent(TestCKCutSamePreviewActivity.this, TestCKCutSameThumbnailActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.test_ck_space_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < CKUtils.videoMaterialNum; i++) {
                    MediaItem processItem = PeacockCKSolution.getInstance().getTaskOrNull(cutSameModel).getMutableMediaItemList().get(i);
                    processItem.setCrop(new ItemCrop(0.4f, 0.4f, 0.6f, 0.6f));
                    templateVideoPreview.updateMedia(i, processItem, PeacockCKSolution.getInstance().getTaskOrNull(cutSameModel).getMutableMediaItemList());
                }
            }
        });

        nextBtn = findViewById(R.id.test_ck_next);
        nextBtn.setOnClickListener(v -> {
        });
        updateTextBtn = findViewById(R.id.test_ck_update_text);
        updateTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                templateVideoPreview.updateText(0, "哈哈哈哈哈哈哈哈");
            }
        });
        updateMediaBtn = findViewById(R.id.test_ck_update_media);
        updateMediaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaItem processItem = templateVideoPreview.getMediaItem(0, PeacockCKSolution.getInstance().getTaskOrNull(cutSameModel).getMutableMediaItemList());
                processItem.setMediaSrcPath("/sdcard/templates/lifeStyle/0.png");
                processItem.setSource("/sdcard/templates/lifeStyle/0.png");
                templateVideoPreview.updateMedia(0, processItem, PeacockCKSolution.getInstance().getTaskOrNull(cutSameModel).getMutableMediaItemList());
            }
        });
        findViewById(R.id.test_ck_set_volume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < CKUtils.videoMaterialNum; i++) {
                    templateVideoPreview.setVolume(PeacockCKSolution.getInstance().getTaskOrNull(cutSameModel).getMutableMediaItemList().get(i), 1);
                }
            }
        });
        findViewById(R.id.test_ck_ddd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PeacockCKSolution.getInstance().fetchCKResources(null);
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
        templateVideoPreview.release();
//        PeacockCKSolution.getInstance().releaseAllTasks();
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
