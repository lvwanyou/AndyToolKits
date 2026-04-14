/*
package com.example.openglexample.activity.template.ck;

import static com.dianping.video.utils.Const.KEY_TEMPLATE_DURATION;
import static com.dianping.video.utils.Const.PEACOCK_CK_CHANNEL;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.dianping.video.model.CutSameModel;
import com.dianping.video.util.ViewUtils;
import com.dianping.video.widget.BaseVideoFrameListView;
import com.dianping.video.widget.CKVideoFrameListView;
import com.example.openglexample.R;
import com.example.openglexample.utils.CKUtils;
import com.meituan.android.cipstorage.CIPStorageCenter;

public class TestCKCutSameThumbnailActivity extends Activity {
    private static final String TAG = TestCKCutSameThumbnailActivity.class.getSimpleName();
    private RelativeLayout relativeLayout;
    private BaseVideoFrameListView mThumbnailView;

    private CutSameModel cutSameModel;

    private static final int GET_FRAME_COUNT = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ck_thumbnail);

        initView();
    }

    private void initView() {
        relativeLayout = findViewById(R.id.test_thumb_list);

        mThumbnailView = new CKVideoFrameListView(getApplicationContext());
        FrameLayout.LayoutParams thumbnailViewParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        thumbnailViewParams.leftMargin = 0;
        thumbnailViewParams.topMargin = 0;
        relativeLayout.addView(mThumbnailView, thumbnailViewParams);

        // 获取屏幕宽度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        mThumbnailView.setThumbSize(100, 80, screenWidth);
        mThumbnailView.post(new Runnable() {
            @Override
            public void run() {
                mThumbnailView.setRadius(ViewUtils.dip2px(getApplicationContext(), 3));
            }
        });
        mThumbnailView.setFrameFetchListener(new BaseVideoFrameListView.OnFrameFetchListener() {
            @Override
            public void onFetchThumbnailListComplete() {
            }

            @Override
            public void onFetchThumbnailFailed(int index) {
            }
        });

        // 获取模版的时长
        long duration = CIPStorageCenter.instance(getApplicationContext(), PEACOCK_CK_CHANNEL).getLong(KEY_TEMPLATE_DURATION + "_" + CKUtils.md5, -1);
        if (duration == -1) {
            CKUtils.getDurationFromPlayer(getApplicationContext(), this.cutSameModel, new CKUtils.GetDurationFromPlayerListener() {
                @Override
                public void onSuccess(long duration) {
                    getThumbnailFrames(duration);
                }

                @Override
                public void onError(String errorMsg) {

                }
            });
        } else {
            getThumbnailFrames(duration);
        }
    }

    public void getThumbnailFrames(long duration) {
        int frameThumbnailInterval = (int) Math.floor(duration / GET_FRAME_COUNT);

        // 解析扫码结果
        boolean usingExistVideoMaterials = getIntent().getBooleanExtra("usingExistVideoMaterials", false);
        if (usingExistVideoMaterials) {
            this.cutSameModel = new CutSameModel(CKUtils.templateUrl, CKUtils.md5, CKUtils.templateType, CKUtils.effectJsonUrl, CKUtils.videoMaterials);
        } else {
            this.cutSameModel = CKUtils.mockCreateCutSameModel(CKUtils.videoMaterialNum, getApplication());
        }

        mThumbnailView.setCKVideo(this.cutSameModel, 0, 0, 1000, frameThumbnailInterval);

//        原始模版对应的缩略图控件
//        mThumbnailView.setVideo("/sdcard/templates/aaa2.mp4", 0, 300, 20000, 1000, null);

        mThumbnailView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((CKVideoFrameListView) mThumbnailView).getThumbnailByIndex(10);
                ((CKVideoFrameListView) mThumbnailView).getThumbnailByIndex(15);
                ((CKVideoFrameListView) mThumbnailView).getThumbnailByIndex(30);
            }
        }, 5000);
    }
}
*/
