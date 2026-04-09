package com.dianping.video.Task;

import static com.dianping.video.utils.Const.KEY_TEMPLATE_DURATION;
import static com.dianping.video.utils.Const.PEACOCK_CK_CHANNEL;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceView;

import com.bytedance.ies.cutsame.veadapter.CompileListener;
import com.bytedance.ies.nle.editor_jni.NLEModel;
import com.cutsame.solution.CutSameSolution;
import com.cutsame.solution.compile.CompileParam;
import com.cutsame.solution.player.CutSamePlayer;
import com.cutsame.solution.player.GetImageListener;
import com.cutsame.solution.source.ComposeSourceListener;
import com.cutsame.solution.source.CutSameSource;
import com.cutsame.solution.source.PrepareSourceListener;
import com.cutsame.solution.source.SourceInfo;
import com.dianping.video.PeacockCKSolution;
import com.dianping.video.bean.EffectResources;
import com.dianping.video.model.CutSameModel;
import com.dianping.video.template.model.material.core.VideoMaterial;
import com.dianping.video.utils.CKHelper;
import com.dianping.video.utils.Const;
import com.google.gson.Gson;
import com.meituan.android.cipstorage.CIPStorageCenter;
import com.sankuai.android.jarvis.Jarvis;
import com.ss.android.ugc.cut_ui.MediaItem;
import com.ss.android.ugc.cut_ui.TextItem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 详细说明：实现单一模版的基于火山引擎的封装 Task，维护预览及模版资源处理相关的状态信息
 *
 * @author lvwanyou
 */
public class CKTask {
    private static final String TAG = "CK-CKTask";
    public static final String ExportSourceMethodName = "exportSource";
    public static final String GetFrameMethodName = "getVideoFrameWithTime";

    // 模版的 md5 作为 taskId
    private String taskId = "";
    // -------------------- 操作状态相关（模版预下载 + 媒体素材合成） --------------------
    /**
     * 判断effectJson及模版资源资源是否完成预下载
     */
    private AtomicBoolean isSourcePrepared = new AtomicBoolean(false);
    /**
     * 判断effectJson及模版资源资源是否完成预下载
     */
    private AtomicBoolean isSourcePreparing = new AtomicBoolean(false);

    /**
     * 判断媒体素材处理是否完成
     * Type parameters:
     * <K> – 模版素材实例对应的id
     * <V> – 该素材实例是否已被处理
     */
    private Pair<String, Boolean> sourceCompressPair = new Pair<>("", false);

    private CutSameSource cutSameSource;

    private CutSameModel cutSameModel = new CutSameModel();

    /**
     * player 实例集合
     * Type parameters:
     * <K> – 模版素材实例对应的id
     * <V> – player 实例
     */
    private HashMap<String, CutSamePlayer> cutSamePlayerMap = new HashMap<>();

    public final ListenerManager<PeacockCKSolution.CKProgressDealSourceListener> ckPrepareSourceListeners = new ListenerManager<>();
    public final ListenerManager<PeacockCKSolution.CKProgressDealSourceListener> ckCompressSourceListeners = new ListenerManager<>();
    public final ListenerManager<PeacockCKSolution.CKProgressDealSourceListener> ckExportSourceListeners = new ListenerManager<>();
    public final ListenerManager<PeacockCKSolution.CKGetImageListener> getImageListeners = new ListenerManager<>();


    /**
     * 媒体素材槽位信息
     */
    private ArrayList<MediaItem> mutableMediaItemList = new ArrayList<>();
    /**
     * 文字素材槽位信息：不使用文字编辑时需传 null
     */
    private ArrayList<TextItem> mutableTextItemList = null;

    public static class ListenerManager<T> {
        private CopyOnWriteArrayList<T> listenerList;

        public CopyOnWriteArrayList<T> getListenerList() {
            return listenerList;
        }

        public ListenerManager() {
            listenerList = new CopyOnWriteArrayList<T>();
        }

        public void registerListener(T listener) {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            }
        }

        public void unRegisterListener(T listener) {
            listenerList.remove(listener);
            listener = null;
        }

        public void unRegisterTaskListeners() {
            listenerList.clear();
            listenerList = null;
        }
    }


    /**
     * 预下载: 下载 effects 素材和 template.zip
     *
     * @param effectJsonUrl effects 素材链接
     */
    public void downloadEffectsAndPrePareSource(String effectJsonUrl) {
        isSourcePreparing.set(true);
        isSourcePrepared.set(false);
        // 进行 template.zip 的下载
        downloadEffectsJson(effectJsonUrl, new DownloadEffectsJsonListener() {
            @Override
            public void onSuccess() {
               CKTask.this.prepareSource();
            }
        });
    }


    /**
     * 下载 effects 素材
     *
     * @param effectJsonUrl
     * @param downloadEffectsJsonListener 下载状态回调
     */
    public void downloadEffectsJson(final String effectJsonUrl, final DownloadEffectsJsonListener downloadEffectsJsonListener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 进行关于 effects 素材 的 json 列表的下载
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request request = new Request.Builder().url(effectJsonUrl).build();
                try {
                    Response response = client.newCall(request).execute();
                    // 解析 Json 数据
                    if (response.body() != null) {
                        String jsonString = response.body().string();
                        Const.effectResources = new Gson().fromJson(jsonString, EffectResources.class);
                    }
                    downloadEffectsJsonListener.onSuccess();
                } catch (IOException e) {
                    CKHelper.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (CKTask.this) {
                                try {
                                    ListIterator<PeacockCKSolution.CKProgressDealSourceListener> iterator = ckPrepareSourceListeners.getListenerList().listIterator();
                                    while (iterator.hasNext()) {
                                        iterator.next().onError(PeacockCKSolution.PeacockCKError.PREPARE_EFFECT_JSON_ERROR.getErrorCode(), PeacockCKSolution.PeacockCKError.PREPARE_EFFECT_JSON_ERROR.getErrorMsg());
                                    }
                                    ckPrepareSourceListeners.getListenerList().clear();
                                } catch (Throwable ignored) {
                                    Log.e(TAG, "ckPrepareSourceListeners iterator.next().onError remove error.");
                                }

                                isSourcePreparing.set(false);
                            }
                        }
                    });
                }
            }
        };
        Jarvis.newThread("CKTask-" + taskId, runnable).start();
    }


    /**
     * 准备模版，解析槽位信息
     */
    private void prepareSource() {
        if (cutSameSource == null) {
            SourceInfo sourceInfo = new SourceInfo(cutSameModel.getTemplateUrl(), cutSameModel.getMd5(), cutSameModel.getTemplateType());
            cutSameSource = CutSameSolution.INSTANCE.createCutSameSource(sourceInfo);
        }

        final long startTime = System.currentTimeMillis();
        cutSameSource.prepareSource(new PrepareSourceListener() {
            @Override
            public void onProgress(float progress) {
                CKHelper.novaCodeLogI(CKTask.class, TAG, "prepareSource - onProgress: " + progress);

                for (PeacockCKSolution.CKProgressDealSourceListener item : ckPrepareSourceListeners.getListenerList()) {
                    item.onProgress(progress);
                }
            }

            @Override
            public void onSuccess(@Nullable ArrayList<MediaItem> mediaItemArrayList, @Nullable ArrayList<TextItem> textItemArrayList, NLEModel nleModel) {
                long endTime = System.currentTimeMillis();
                CKHelper.novaCodeLogI(CKTask.class, TAG, "prepareSource - onSuccess");
                CKHelper.novaCodeLogI(CKTask.class, TAG, "prepareSource - time consuming: " + (endTime - startTime));

                if (mediaItemArrayList == null) {
                    return;
                }

                mutableMediaItemList.clear();
                for (MediaItem mediaItem : mediaItemArrayList) {
                    if (mediaItem.isMutable()) {
                        mutableMediaItemList.add(mediaItem);
                    }
                }

                CKHelper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (CKTask.this) {
                            try {
                                ListIterator<PeacockCKSolution.CKProgressDealSourceListener> iterator = ckPrepareSourceListeners.getListenerList().listIterator();
                                while (iterator.hasNext()) {
                                    iterator.next().onSuccess();
                                }
                                ckPrepareSourceListeners.getListenerList().clear();
                            } catch (Throwable ignored) {
                                Log.e(TAG, "ckPrepareSourceListeners iterator.next().onSuccess remove error.");
                            }

                            isSourcePreparing.set(false);
                            isSourcePrepared.set(true);
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
                CKHelper.novaCodeLogE(CKTask.class, TAG, "prepareSource - onError ： " + errorCode + " ： " + errorMsg);

                CKHelper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (CKTask.this) {
                            try {
                                ListIterator<PeacockCKSolution.CKProgressDealSourceListener> iterator = ckPrepareSourceListeners.getListenerList().listIterator();
                                while (iterator.hasNext()) {
                                    iterator.next().onError(PeacockCKSolution.PeacockCKError.PREPARE_SOURCE_ERROR.getErrorCode(), PeacockCKSolution.PeacockCKError.PREPARE_SOURCE_ERROR.getErrorMsg());
                                }
                                ckPrepareSourceListeners.getListenerList().clear();
                            } catch (Throwable ignored) {
                                Log.e(TAG, "ckPrepareSourceListeners iterator.next().onError remove error.");
                            }

                            isSourcePreparing.set(false);
                        }
                    }
                });
            }
        });
    }


    /**
     * 媒体素材处理
     *
     * @param mediaItems 带素材路径的槽位信息
     */
    public void composeSource(final ArrayList<MediaItem> mediaItems) {
        sourceCompressPair = new Pair<>(getCutSameModelId(), false);
        if (cutSameSource != null) {
            final long startTime = System.currentTimeMillis();
            cutSameSource.composeSource(mediaItems, new ComposeSourceListener() {
                @Override
                public void onProgress(float progress) {
                    if (Const.isDebug) {
                        Log.e(TAG, "composeSource - onProgress: " + progress);
                    }

                    for (PeacockCKSolution.CKProgressDealSourceListener item : ckCompressSourceListeners.getListenerList()) {
                        item.onProgress(progress);
                    }
                }

                @Override
                public void onSuccess(ArrayList<MediaItem> arrayList) {
                    long endTime = System.currentTimeMillis();
                    if (Const.isDebug) {
                        for (MediaItem mediaItem: mediaItems) {
                            Log.e(TAG, "composeSource - set: " + mediaItem.mediaSrcPath);
                        }
                    }
                    CKHelper.novaCodeLogI(CKTask.class, TAG, "composeSource - onSuccess");
                    CKHelper.novaCodeLogI(CKTask.class, TAG, "composeSource - time consuming: " + (endTime - startTime));

                    setMutableMediaItemList(arrayList);
                    CKHelper.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (CKTask.this) {
                                try {
                                    ListIterator<PeacockCKSolution.CKProgressDealSourceListener> iterator = ckCompressSourceListeners.getListenerList().listIterator();
                                    while (iterator.hasNext()) {
                                        iterator.next().onSuccess();
                                    }
                                    ckCompressSourceListeners.getListenerList().clear();
                                } catch (Throwable ignored) {
                                    Log.e(TAG, "ckCompressSourceListeners iterator.next().onSuccess remove error.");
                                }

                                sourceCompressPair = new Pair<>(getCutSameModelId(), true);
                            }
                        }
                    });
                }

                @Override
                public void onError(final int errorCode, final String errorMsg) {
                    CKHelper.novaCodeLogE(CKTask.class, TAG, "composeSource - onError; errorCode=" + errorCode + " ;errorMsg : " + errorMsg);

                    CKHelper.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (CKTask.this) {
                                try {
                                    ListIterator<PeacockCKSolution.CKProgressDealSourceListener> iterator = ckCompressSourceListeners.getListenerList().listIterator();
                                    while (iterator.hasNext()) {
                                        iterator.next().onError(errorCode, errorMsg);
                                    }
                                    ckCompressSourceListeners.getListenerList().clear();
                                } catch (Throwable ignored) {
                                    Log.e(TAG, "ckCompressSourceListeners iterator.next().onError remove error.");
                                }

                                sourceCompressPair = new Pair<>(getCutSameModelId(), false);
                            }
                        }
                    });
                }
            });
        }
    }


    /**
     * 视频模版成品导出
     *
     * @param mContext
     * @param outputVideoPath
     * @param param
     */
    public void exportSource(Context mContext, String outputVideoPath, CompileParam param) {
        final String exportSourcePlayerId = getCutSameModelId() + ExportSourceMethodName;
        CutSamePlayer cutSamePlayer = cutSamePlayerMap.get(exportSourcePlayerId);
        if (cutSamePlayer == null) {
            cutSamePlayer = createCutSamePlayer(new SurfaceView(mContext), cutSameSource);
            if (cutSamePlayer != null) {
                cutSamePlayerMap.put(exportSourcePlayerId, cutSamePlayer);
                preparePlay(cutSamePlayer, mutableMediaItemList, mutableTextItemList, mContext);
            } else {
                synchronized (CKTask.this) {
                    try {
                        ListIterator<PeacockCKSolution.CKProgressDealSourceListener> iterator = ckExportSourceListeners.getListenerList().listIterator();
                        while (iterator.hasNext()) {
                            iterator.next().onError(PeacockCKSolution.PeacockCKError.CK_CREATE_PLAYER_ERROR.getErrorCode(), PeacockCKSolution.PeacockCKError.CK_CREATE_PLAYER_ERROR.getErrorMsg());
                        }
                        ckExportSourceListeners.getListenerList().clear();
                    } catch (Throwable ignored) {
                        CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "ckExportSourceListeners iterator.next().onError remove error.");
                    }
                }

                return;
            }
        }
        final CutSamePlayerStatusListener playerStatusListener = new CutSamePlayerStatusListener() {
            @Override
            public void onPlayerDone() {
                releasePlayer(exportSourcePlayerId);
            }
        };

        final long startTime = System.currentTimeMillis();
        cutSamePlayer.compile(outputVideoPath, param, new CompileListener() {
            @Override
            public void onCompileError(final int errorCode, int ext, float v, final String errorMsg) {
                CKHelper.novaCodeLogE(CKTask.class, TAG, "onCompileError");

                CKHelper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (CKTask.this) {
                            try {
                                ListIterator<PeacockCKSolution.CKProgressDealSourceListener> iterator = ckExportSourceListeners.getListenerList().listIterator();
                                while (iterator.hasNext()) {
                                    iterator.next().onError(errorCode, errorMsg);
                                }
                                ckExportSourceListeners.getListenerList().clear();
                            } catch (Throwable ignored) {
                                CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "ckExportSourceListeners iterator.next().onError remove error.");
                            }
                        }
                    }
                });

                playerStatusListener.onPlayerDone();
            }

            @Override
            public void onCompileDone() {
                long endTime = System.currentTimeMillis();
                CKHelper.novaCodeLogI(CKTask.class, TAG, "exportSource - onSuccess");
                CKHelper.novaCodeLogI(CKTask.class, TAG, "exportSource - time consuming: " + (endTime - startTime));

                CKHelper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (CKTask.this) {
                            try {
                                ListIterator<PeacockCKSolution.CKProgressDealSourceListener> iterator = ckExportSourceListeners.getListenerList().listIterator();
                                while (iterator.hasNext()) {
                                    iterator.next().onSuccess();
                                }
                                ckExportSourceListeners.getListenerList().clear();
                            } catch (Throwable ignored) {
                                CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "ckExportSourceListeners iterator.next().onSuccess remove error.");
                            }
                        }
                    }
                });

                playerStatusListener.onPlayerDone();
            }

            @Override
            public void onCompileProgress(float progress) {
                if (Const.isDebug) {
                    Log.e(TAG, "onCompileProgress: " + progress);
                }

                for (PeacockCKSolution.CKProgressDealSourceListener item : ckExportSourceListeners.getListenerList()) {
                    item.onProgress(progress);
                }
            }
        });
    }


    /**
     * 获取时间点的截图
     *
     * @param mContext
     * @param timeStamps
     * @param thumbnailWidth
     * @param thumbnailHeight
     */
    public void getVideoFrameWithTime(Context mContext, int[] timeStamps, int thumbnailWidth, int thumbnailHeight) {
        final String getFramePlayerId = getCutSameModelId() + GetFrameMethodName;
        final CutSamePlayer[] cutSamePlayers = {cutSamePlayerMap.get(getFramePlayerId)};
        if (cutSamePlayers[0] == null) {
            cutSamePlayers[0] = createCutSamePlayer(new SurfaceView(mContext), cutSameSource);
            if (cutSamePlayers[0] != null) {
                cutSamePlayerMap.put(getFramePlayerId, cutSamePlayers[0]);
                preparePlay(cutSamePlayers[0], mutableMediaItemList, mutableTextItemList, mContext);
            } else {
                synchronized (CKTask.this) {
                    try {
                        ListIterator<PeacockCKSolution.CKGetImageListener> iterator = getImageListeners.getListenerList().listIterator();
                        while (iterator.hasNext()) {
                            iterator.next().onError(PeacockCKSolution.PeacockCKError.CK_CREATE_PLAYER_ERROR.getErrorCode(), PeacockCKSolution.PeacockCKError.CK_CREATE_PLAYER_ERROR.getErrorMsg());
                        }
                        getImageListeners.getListenerList().clear();
                    } catch (Throwable ignored) {
                        Log.e(TAG, "getImageListeners iterator.next().onError remove error.");
                    }
                }

                return;
            }
        }
        final CutSamePlayerStatusListener playerStatusListener = new CutSamePlayerStatusListener() {
            @Override
            public void onPlayerDone() {
                releasePlayer(getFramePlayerId);
            }
        };

        final long startTime = System.currentTimeMillis();
        cutSamePlayers[0].getVideoFrameWithTime(timeStamps,
                thumbnailWidth,
                thumbnailHeight,
                new GetImageListener() {
                    @Override
                    public void onGetImageData(byte[] bytes, int pts, int width, int height, float score) {
                        if (bytes != null) {
                            if (Const.isDebug) {
                                Log.e(TAG, "getVideoFrameWithTime - pts: " + pts);
                            }

                            Bitmap bitmap = null;
                            try {
                                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));
                            } catch (Throwable ignored) {
                                Log.e(TAG, "getVideoFrameWithTime - createBitmap error!!");
                            }

                            for (PeacockCKSolution.CKGetImageListener item : getImageListeners.getListenerList()) {
                                item.frameBitmap(String.valueOf(pts), bitmap);
                            }
                        } else {
                            long endTime = System.currentTimeMillis();
                            CKHelper.novaCodeLogI(CKTask.class, TAG, "getVideoFrameWithTime - onSuccess");
                            CKHelper.novaCodeLogI(CKTask.class, TAG, "getVideoFrameWithTime - time consuming: " + (endTime - startTime));

                            // 取帧结束后需要cancel，不然会停留在取帧状态
                            cutSamePlayers[0].cancelGetVideoFrames();

                            CKHelper.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (CKTask.this) {
                                        try {
                                            ListIterator<PeacockCKSolution.CKGetImageListener> iterator = getImageListeners.getListenerList().listIterator();
                                            while (iterator.hasNext()) {
                                                iterator.next().frameBitmap("", null);
                                            }
                                            getImageListeners.getListenerList().clear();
                                        } catch (Throwable ignored) {
                                            Log.e(TAG, "getImageListeners iterator.next().frameBitmap remove error.");
                                        }
                                    }
                                }
                            });

                            playerStatusListener.onPlayerDone();
                        }
                    }
                });
    }


    /**
     * 创建 CutSamePlayer
     *
     * @param surfaceView
     * @param importSource
     * @return
     */
    public CutSamePlayer createCutSamePlayer(SurfaceView surfaceView, CutSameSource importSource) {
        if (importSource == null) {
            if (!cutSameModel.getTemplateUrl().isEmpty() && !cutSameModel.getMd5().isEmpty()) {
                SourceInfo sourceInfo = new SourceInfo(cutSameModel.getTemplateUrl(), cutSameModel.getMd5(), cutSameModel.getTemplateType());
                importSource = CutSameSolution.INSTANCE.createCutSameSource(sourceInfo);
            } else {
                return null;
            }
        }

        return CutSameSolution.INSTANCE.createCutSamePlayer(surfaceView, importSource);
    }

    /**
     * 准备播放
     *
     * @param cutSamePlayer
     * @param mutableMediaItemList
     * @param mutableTextItemList
     */
    public void preparePlay(CutSamePlayer cutSamePlayer, ArrayList<MediaItem> mutableMediaItemList, ArrayList<TextItem> mutableTextItemList, Context mContext) {
        cutSamePlayer.preparePlay(mutableMediaItemList, mutableTextItemList);

        String keyOfTemplateDuration = KEY_TEMPLATE_DURATION + "_" + cutSameModel.getMd5();
        if (CIPStorageCenter.instance(mContext, PEACOCK_CK_CHANNEL).getLong(keyOfTemplateDuration, -1) == -1) {
            CIPStorageCenter.instance(mContext, PEACOCK_CK_CHANNEL).setLong(keyOfTemplateDuration, cutSamePlayer.getDuration());
        }
    }

    public void releaseSource() {
        if (cutSameSource != null) {
            cutSameSource.release();
            cutSameSource = null;
        }
        isSourcePrepared.set(false);
    }

    public void releasePlayer(String playerId) {
        if (cutSamePlayerMap.containsKey(playerId)) {
            CutSamePlayer cutSamePlayer = cutSamePlayerMap.get(playerId);
            cutSamePlayerMap.remove(playerId);
            if (cutSamePlayer != null) {
                cutSamePlayer.release();
            }
        }
    }

    public void releasePlayers() {
        for (String playerId : cutSamePlayerMap.keySet()) {
            releasePlayer(playerId);
        }

        cutSamePlayerMap.clear();
    }

    public void release() {
        cutSameModel = null;

        releaseSource();
        releasePlayers();

        getMutableMediaItemList().clear();
        getMutableTextItemList().clear();

        ckPrepareSourceListeners.unRegisterTaskListeners();
        ckCompressSourceListeners.unRegisterTaskListeners();
        ckExportSourceListeners.unRegisterTaskListeners();
        getImageListeners.unRegisterTaskListeners();
    }

    /**
     * 判断 模版预下载 + 媒体素材合成 是否完成
     *
     * @return 标识
     */
    public boolean isModelPreparedAndComposed() {
        return isSourcePrepared.get() && isSourceCompressed();
    }

    public String getCutSameModelId() {
        return getCutSameModelId(this.cutSameModel);
    }

    public String getCutSameModelId(CutSameModel cutSameModel) {
        StringBuilder playerId = new StringBuilder(taskId);
        if (cutSameModel.getVideoMaterials() != null && !cutSameModel.getVideoMaterials().isEmpty()) {
            for (VideoMaterial videoMaterial : cutSameModel.getVideoMaterials()) {
                if (!TextUtils.isEmpty(videoMaterial.getPath())) {
                    playerId.append(videoMaterial.getPath().hashCode());
                }
            }
        }
        return String.valueOf(playerId);
    }

    public CutSamePlayer getCutSamePlayer(String playerId) {
        return cutSamePlayerMap.get(playerId);
    }

    public void setCutSamePlayer(String playerId, CutSamePlayer cutSamePlayer) {
        if (!cutSamePlayerMap.containsKey(playerId)) {
            cutSamePlayerMap.put(playerId, cutSamePlayer);
        }
    }


    public ArrayList<MediaItem> getMutableMediaItemList() {
        return mutableMediaItemList;
    }

    public void setMutableMediaItemList(ArrayList<MediaItem> mutableMediaItemList) {
        this.mutableMediaItemList = mutableMediaItemList;
    }

    public ArrayList<TextItem> getMutableTextItemList() {
        return mutableTextItemList;
    }

    public void setMutableTextItemList(ArrayList<TextItem> mutableTextItemList) {
        this.mutableTextItemList = mutableTextItemList;
    }

    public boolean isSourcePreparing() {
        return isSourcePreparing.get();
    }

    public boolean isSourcePrepared() {
        return isSourcePrepared.get();
    }

    public boolean isSourceCompressed() {
        return sourceCompressPair.first.equals(getCutSameModelId()) && sourceCompressPair.second;
    }

    public CutSameSource getCutSameSource() {
        return cutSameSource;
    }

    public void setCutSameSource(CutSameSource cutSameSource) {
        this.cutSameSource = cutSameSource;
    }

    public CutSameModel getCutSameModel() {
        return cutSameModel;
    }

    public void setCutSameModel(CutSameModel cutSameModel) {
        this.cutSameModel = cutSameModel;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public interface CutSamePlayerStatusListener {
        void onPlayerDone();
    }

    public interface DownloadEffectsJsonListener {
        void onSuccess();
    }
}
