package com.dianping.video.widget;

import static com.dianping.video.utils.Const.KEY_TEMPLATE_DURATION;
import static com.dianping.video.utils.Const.PEACOCK_CK_CHANNEL;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import com.cutsame.solution.player.CutSamePlayer;
import com.dianping.video.PeacockCKSolution;
import com.dianping.video.Task.CKTask;
import com.dianping.video.model.CutSameModel;
import com.dianping.video.utils.CKHelper;
import com.dianping.video.utils.CKVideoFrameProvider;
import com.meituan.android.cipstorage.CIPStorageCenter;

/**
 * 剪同款视频帧缩略图控件
 *
 * @author lvwanyou
 * @api
 */
public class CKVideoFrameListView extends BaseVideoFrameListView {

    private static final String TAG = "CK-CKVideoFrameListView";

    /**
     * 缩略图控件对应的任务身份标识
     */
    private CutSameModel cutSameModel;

    /**
     * 用于控件封面展示的帧间隔
     */
    private int mCoverThumbnailInterval = 1000;

    public CKVideoFrameListView(Context context) {
        super(context);
    }

    public CKVideoFrameListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CKVideoFrameListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setCKVideo(Object cutSameModel, final int selectedFrameTime, final int clipVideoStartTime, int coverThumbnailInterval, int frameThumbnailInterval) {
        if (!(cutSameModel instanceof CutSameModel)) return;
        this.cutSameModel = (CutSameModel) cutSameModel;
        mVideoType = VIDEO_TYPE_CK_TEMPLATE;
        mThumbnailInterval = frameThumbnailInterval;
        mCoverThumbnailInterval = coverThumbnailInterval;

        // 获取模版的时长
        long duration = CIPStorageCenter.instance(getContext(), PEACOCK_CK_CHANNEL).getLong(KEY_TEMPLATE_DURATION + "_" + this.cutSameModel.getMd5(), -1);
        if (duration == -1) {
            getDurationFromPlayer(new GetDurationFromPlayerListener() {
                @Override
                public void onSuccess(long duration) {
                    setupAdapter(selectedFrameTime, clipVideoStartTime, (int) duration, null);
                }

                @Override
                public void onError(String errorMsg) {
                }
            });
        } else {
            setupAdapter(selectedFrameTime, clipVideoStartTime, (int) duration, null);
        }

    }

    @Override
    public void setupAdapter(int selectedFrameTime, final int clipVideoStart, final int duration, String privacyToken) {
        if (mVideoType == VIDEO_TYPE_CK_TEMPLATE) {
            int coverFrameCount = duration / mCoverThumbnailInterval;

            CKVideoFrameProvider frameProvider = new CKVideoFrameProvider(this.getContext(), cutSameModel, mThumbWidth, mThumbHeight, duration, mThumbnailInterval, mCoverThumbnailInterval, clipVideoStart);
            if (reporter != null) {
                frameProvider.setFrameReporter(reporter);
            }
            mAdapter = new FrameListAdapter(frameProvider, coverFrameCount, mThumbWidth, mThumbHeight, mThumbFillWidth);
            if (mHolderImage != null && !mHolderImage.isRecycled()) {
                mAdapter.setImagePlaceHolder(mHolderImage);
            }
            mThumbListView.setAdapter(mAdapter);
            frameProvider.setFetchFrameCompleteListener(new CKVideoFrameProvider.OnFetchFrameCompleteListener() {
                @Override
                public void onFetchThumbnailComplete(final int index, Bitmap bitmap) {
                    mThumbListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.onFetchThumbnailComplete(index);
                        }
                    });
                }

                @Override
                public void onFetchThumbnailListComplete() {
                    if (mFrameFetchListener != null) {
                        mFrameFetchListener.onFetchThumbnailListComplete();
                    }
                }

                @Override
                public void onFetchThumbnailFailed(int index) {
                    if (mFrameFetchListener != null) {
                        mFrameFetchListener.onFetchThumbnailFailed(index);
                    }
                }
            });
        }

        final double durationPerPixel = (double) mThumbnailInterval / mThumbWidth;

        mThumbListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollPosition += dx * durationPerPixel;
                if (mScrollPosition < 0) {
                    mScrollPosition = 0;
                }
                if (mScrollPosition > duration + clipVideoStart) {
                    mScrollPosition = duration + clipVideoStart;
                }
                if (mScrollListener != null) {
                    mScrollListener.onScrollTo(mScrollPosition);
                }
            }
        });

        mThumbListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mScrollListener != null) {
                        mScrollListener.onScrollStart();
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mScrollListener != null) {
                        mScrollListener.onScrollEnd();
                    }
                }
                return false;
            }
        });

        mScrollPosition = selectedFrameTime;

        int scrollPixel = (int) (mScrollPosition / durationPerPixel);
        int scrollPosition = (scrollPixel) / mThumbWidth + 1;
        int scrollOffset = mThumbFillWidth - (scrollPixel) % mThumbWidth;

        mLayoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset);
    }

    public void getDurationFromPlayer(final GetDurationFromPlayerListener getDurationFromPlayerListener) {
        final CKTask ckTask = PeacockCKSolution.getInstance().getTaskOrNew(this.cutSameModel);
        if (ckTask != null) {
            PeacockCKSolution.getInstance().prepareAndCompressTemplateSource(ckTask.getCutSameModel(), new PeacockCKSolution.CKProgressDealSourceListener() {
                @Override
                public void onProgress(float progress) {

                }

                @Override
                public void onSuccess() {
                    CutSamePlayer cutSamePlayer = ckTask.createCutSamePlayer(new SurfaceView(CKVideoFrameListView.this.getContext()), ckTask.getCutSameSource());
                    if (cutSamePlayer != null) {
                        ckTask.setCutSamePlayer(ckTask.getCutSameModelId() + CKTask.GetFrameMethodName, cutSamePlayer);
                        ckTask.preparePlay(cutSamePlayer, ckTask.getMutableMediaItemList(), ckTask.getMutableTextItemList(), getContext());
                        getDurationFromPlayerListener.onSuccess(cutSamePlayer.getDuration());
                    } else {
                        getDurationFromPlayerListener.onError("create player error.");
                    }
                }

                @Override
                public void onError(int errorCode, @Nullable String errorMsg) {
                    getDurationFromPlayerListener.onError(errorMsg);
                }
            });
        } else {
            getDurationFromPlayerListener.onError("get CKTask fail when setupAdapter, need call PeacockCKSolution.getInstance().prepareTemplateSource()!");

            CKHelper.novaCodeLogE(CKVideoFrameListView.class, TAG, "get CKTask fail when setupAdapter, need call PeacockCKSolution.getInstance().prepareTemplateSource()!");
        }
    }

    /**
     * 获取指定帧图像
     *
     * @param index
     * @return Bitmap
     * @api
     */
    public Bitmap getThumbnailByIndex(int index) {
        return mAdapter != null && mAdapter.mVideoFrameProvider != null ? ((CKVideoFrameProvider) (mAdapter.mVideoFrameProvider)).getFrameThumbnailByIndex(index - 1) : null;
    }

    interface GetDurationFromPlayerListener {
        void onSuccess(long duration);

        void onError(String errorMsg);
    }
}
