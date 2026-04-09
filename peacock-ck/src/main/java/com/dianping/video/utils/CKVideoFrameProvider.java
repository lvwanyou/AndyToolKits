package com.dianping.video.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.dianping.video.PeacockCKSolution;
import com.dianping.video.model.CutSameModel;
import com.dianping.video.template.constant.TemplateErrorCode;
import com.dianping.video.template.monitor.ITemplateFrameReporter;
import com.dianping.video.template.monitor.TemplateFrameReportItem;
import com.dianping.video.util.VideoFrameProvider;
import com.sankuai.android.jarvis.Jarvis;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * 剪同款视频帧获取工具
 *
 * @author wuxuelei & lvwanyou
 * @api
 */
public class CKVideoFrameProvider extends VideoFrameProvider {
    private final CutSameModel cutSameModel;
    private final int mThumbnailHeight;
    private final int mThumbnailWidth;
    private final int mThumbnailInterval;
    private final int mCoverThumbnailInterval;
    private int frameCount = 0;
    private int coverFrameCount = 0;
    private final int mThumbnailStartTime;
    private ExecutorService mExecutorService = Jarvis.newSingleThreadExecutor("CutSameVideoFrameProvider");
    private PeacockCKSolution.CKGetImageListener mCKGetImageListener = null;
    private Context mContext;

    /**
     * 封面缓存池
     */
    protected final HashMap<Integer, Bitmap> mCoverCache = new HashMap<>();

    //监控相关
    private ITemplateFrameReporter reporter;
    private final TemplateFrameReportItem reportItem = new TemplateFrameReportItem(TemplateFrameReportItem.TemplateType.CK_TEMPLATE);

    /**
     * 获取工具实例初始化
     *
     * @param mContext
     * @param cutSameModel
     * @param thumbnailWidth
     * @param thumbnailHeight
     * @param duration
     * @param thumbnailInterval
     * @param coverThumbnailInterval
     * @param thumbnailStartTime
     */
    public CKVideoFrameProvider(Context mContext, CutSameModel cutSameModel, int thumbnailWidth, int thumbnailHeight, int duration, int thumbnailInterval, int coverThumbnailInterval, int thumbnailStartTime) {
        this.mContext = mContext;
        this.cutSameModel = cutSameModel;
        this.mThumbnailHeight = thumbnailHeight;
        this.mThumbnailWidth = thumbnailWidth;
        this.mThumbnailInterval = thumbnailInterval;
        this.mCoverThumbnailInterval = coverThumbnailInterval;
        this.frameCount = duration / mThumbnailInterval;
        this.coverFrameCount = duration / mCoverThumbnailInterval;
        this.mThumbnailStartTime = thumbnailStartTime;

        reportItem.clear();
    }

    private class FetchThumbnailTask implements Runnable {
        private final int[] times;
        private int thumbnailInterval;
        private int coverThumbnailInterval;
        private int lastCoverFetchIndex = -1;

        private FetchThumbnailTask(int[] times, int thumbnailInterval, int coverThumbnailInterval) {
            this.times = times;
            this.thumbnailInterval = thumbnailInterval;
            this.coverThumbnailInterval = coverThumbnailInterval;
        }

        @Override
        public void run() {
            final long batchFrameStartTime = System.currentTimeMillis();
            mCKGetImageListener = new PeacockCKSolution.CKGetImageListener() {
                @Override
                public void frameBitmap(String timeStamp, final Bitmap bitmap) {
                    int pts = !timeStamp.isEmpty() ? Integer.parseInt(timeStamp) : 0;
                    final int fetchIndex = (pts - mThumbnailStartTime) / thumbnailInterval;
                    final int coverFetchIndex = (pts - mThumbnailStartTime) / coverThumbnailInterval;

                    if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                        reportItem.realFrameCount++;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCache.put(fetchIndex, bitmap);
                                mFetchingLists.add(fetchIndex);
                                if (mFetchFrameCompleteListener != null) {
                                    if (lastCoverFetchIndex != coverFetchIndex) {
                                        mCoverCache.put(coverFetchIndex, bitmap);
                                        lastCoverFetchIndex = coverFetchIndex;

                                        mFetchFrameCompleteListener.onFetchThumbnailComplete(coverFetchIndex, bitmap);
                                    }
                                }
                                if (frameCount > 0) {
                                    if (mCache.size() == frameCount && mFetchFrameCompleteListener != null) {
                                        mFetchFrameCompleteListener.onFetchThumbnailListComplete();
                                    }
                                }
                            }
                        });
                    } else {
                        monitor(batchFrameStartTime, timeStamp.isEmpty());

                        // 抽帧结束后，将运行池清空
                        if (timeStamp.isEmpty()) {
                            mFetchingLists.clear();
                        }
                        if (mFetchFrameCompleteListener != null && !timeStamp.isEmpty()) {
                            mFetchFrameCompleteListener.onFetchThumbnailFailed(fetchIndex);
                        }
                    }
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                }
            };
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
            PeacockCKSolution.getInstance().getVideoFrameWithTime(mContext, cutSameModel, times, (int) (displayMetrics.widthPixels / 2), -1, mCKGetImageListener);
        }
    }

    /**
     * 监控上报
     *
     * @param startTime 起始时间
     */
    private void monitor(long startTime, boolean hasFetchThumbnailSucc) {
        if (reporter != null) {
            reportItem.frameAvgCostTime = reportItem.realFrameCount > 0 ? (System.currentTimeMillis() - startTime) / reportItem.realFrameCount : (System.currentTimeMillis() - startTime);
            reportItem.resultCode = hasFetchThumbnailSucc ? 200 : TemplateErrorCode.VIDEO_GET_BATCH_FRAMES_ERROR;
            reportItem.idealFrameCount = frameCount;
            reporter.report(reportItem);
            reportItem.clear();
        }
    }


    public Bitmap getFrameThumbnailByIndex(int index) {
        if (mCache.containsKey(index)) {
            return mCache.get(index);
        } else {
            synchronized (mFetchingLists) {
                if (!mFetchingLists.isEmpty()) {
                    return null;
                }
                mFetchingLists.add(index);
            }

            int[] times = new int[frameCount];
            int fetchTime = mThumbnailStartTime;
            for (int i = 0; i < frameCount; i++) {
                times[i] = fetchTime;
                fetchTime += mThumbnailInterval;
            }

            if (mExecutorService != null && !mExecutorService.isShutdown()) {
                mExecutorService.submit(new FetchThumbnailTask(times, mThumbnailInterval, mCoverThumbnailInterval));
            }
        }
        return mCache.get(index);
    }

    @Override
    public Bitmap getThumbnailByIndex(int index) {
        if (mCoverCache.containsKey(index)) {
            return mCoverCache.get(index);
        } else {
            synchronized (mFetchingLists) {
                if (!mFetchingLists.isEmpty()) {
                    return null;
                }
                mFetchingLists.add(index);
            }

            int[] times = new int[frameCount];
            int fetchTime = mThumbnailStartTime;
            for (int i = 0; i < frameCount; i++) {
                times[i] = fetchTime;
                fetchTime += mThumbnailInterval;
            }

            if (mExecutorService != null && !mExecutorService.isShutdown()) {
                mExecutorService.submit(new FetchThumbnailTask(times, mThumbnailInterval, mCoverThumbnailInterval));
            }
        }
        return mCoverCache.get(index);
    }

    @Override
    public void exit() {
        if (!mExecutorService.isShutdown()) {
            mExecutorService.shutdown();
        }

        if (mCKGetImageListener != null) {
            PeacockCKSolution.getInstance().unRegisterGetViewFrameListener(cutSameModel, mCKGetImageListener);
        }
    }

    /**
     * 设置监控
     *
     * @param reporter 监控类
     */
    public void setFrameReporter(ITemplateFrameReporter reporter) {
        this.reporter = reporter;
    }
}
