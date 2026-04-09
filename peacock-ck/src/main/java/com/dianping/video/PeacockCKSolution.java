package com.dianping.video;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bytedance.ies.cutsame.util.UriAdapterSwitch;
import com.cutsame.solution.AuthorityConfig;
import com.cutsame.solution.CutSameSolution;
import com.cutsame.solution.EffectFetcherConfig;
import com.cutsame.solution.TemplateFetcherConfig;
import com.cutsame.solution.compile.CompileParam;
import com.cutsame.solution.player.CutSamePlayer;
import com.dianping.video.Task.CKTask;
import com.dianping.video.model.CKCompileParam;
import com.dianping.video.model.CutSameModel;
import com.dianping.video.template.model.material.core.VideoMaterial;
import com.dianping.video.util.MediaUtils;
import com.dianping.video.utils.CKHelper;
import com.dianping.video.utils.Const;
import com.dianping.video.utils.DataHelper;
import com.ss.android.ugc.cut_log.LogConfig;
import com.ss.android.ugc.cut_log.LogIF;
import com.ss.android.ugc.cut_log.LogWrapper;
import com.ss.android.ugc.cut_ui.MediaItem;
import com.ss.android.vesdk.VEConfigCenter;
import com.ss.android.vesdk.VEConfigKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeacockCKSolution {

    private static final String TAG = "CK-PeacockCKSolution";

    // 私有静态变量，存储唯一实例
    private static volatile PeacockCKSolution instance;

    private Context mContext;
    private String privacyToken;

    private final CKResourcesManager ckResourcesManager = new CKResourcesManager();
    private final HashMap<String, CKTask> ckTaskManager = new HashMap<>();

    private final CKNativeLibsManager ckNativeLibsManager = new CKNativeLibsManager();

    // -------------------- 初始化状态相关（鉴权文件及模型文件下载 + 引擎初始化） --------------------
    /**
     * 是否正在进行初始化
     */
    private final AtomicBoolean isPeacockSolutionInitPreparing = new AtomicBoolean(false);
    /**
     * 是否初始化成功，包括: 1.鉴权文件及模型文件下载 2.引擎初始化
     */
    private final AtomicBoolean isPeacockSolutionInit = new AtomicBoolean(false);
    private final CopyOnWriteArrayList<CKInitListener> watcherInitList = new CopyOnWriteArrayList<>();
    /**
     * peacock-ck 初始化状态失败的错误码
     */
    private int ckInitErrorCode = 0;


    // 私有构造方法，禁止外部创建实例
    private PeacockCKSolution() {
        // 加载 so
        ckNativeLibsManager.loadNativeLibs(null);

        CKHelper.novaCodeLogI(PeacockCKSolution.class, TAG, "PeacockCKSolution.INSTANCE.init && so libs try to load.");
    }


    public static PeacockCKSolution getInstance() {
        if (instance == null) {
            synchronized (PeacockCKSolution.class) {
                if (instance == null) {
                    instance = new PeacockCKSolution();
                }
            }
        }
        return instance;
    }


    /**
     * 加载 so
     *
     * @param cKLoadNativeLibsListener so 库加载回调
     */
    public void fetchCKNativeLibs(CKNativeLibsManager.CKLoadNativeLibsListener cKLoadNativeLibsListener) {
        ckNativeLibsManager.loadNativeLibs(cKLoadNativeLibsListener);
    }


    /**
     * 鉴权文件及模型文件下载
     *
     * @param ckDownloadResourcesListener 资源文件下载回调
     */
    public void fetchCKResources(CKResourcesManager.CKDownloadResourcesListener ckDownloadResourcesListener) {
        ckResourcesManager.downLoadCKResources(ckDownloadResourcesListener);
    }


    /**
     * peacock-ck 初始化
     *
     * @param mContext  上下文
     * @param ckInitListener 初始化回调
     */
    public void init(final Context mContext, final String privacyToken, final CKInitListener ckInitListener) {
        if (mContext == null) {
            if (ckInitListener != null) {
                ckInitListener.onError(PeacockCKError.IDLE_ERROR.getErrorCode(), PeacockCKError.IDLE_ERROR.getErrorMsg());
            }
            return;
        }
        this.mContext = mContext;
        this.privacyToken = TextUtils.isEmpty(privacyToken) ? Const.UGC_PRIVACY_TOKEN_NOTE : privacyToken;
        if (ckInitListener != null && !watcherInitList.contains(ckInitListener)) {
            watcherInitList.add(ckInitListener);
        }

        // 避免重复初始化
        if (isPeacockSolutionInit.get()) {
            if (ckInitListener != null) {
                ckInitListener.onSuccess();
            }
            return;
        }

        if (isPeacockSolutionInitPreparing.getAndSet(true)) {
            if (ckInitListener != null) {
                ckInitListener.onInitPreparing();
            }
            return;
        }

        // so 库列表加载
        if (ckNativeLibsManager.getCKFetchNativeLibsFlag() == CKNativeLibsManager.CKFetchNativeLibsFlag.SOS_LOADED) {
            tryFetchCKResources();
        } else {
            fetchCKNativeLibs(new CKNativeLibsManager.CKLoadNativeLibsListener() {
                @Override
                public void onSuccess() {
                    CKHelper.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tryFetchCKResources();
                        }
                    });
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    isPeacockSolutionInit.set(false);
                    isPeacockSolutionInitPreparing.set(false);

                    ckInitErrorCode = PeacockCKError.CK_NATIVE_LIBS_ERROR.getErrorCode();

                    synchronized (PeacockCKSolution.this) {
                        try {
                            ListIterator<CKInitListener> iterator = watcherInitList.listIterator();
                            while (iterator.hasNext()) {
                                iterator.next().onError(PeacockCKError.CK_NATIVE_LIBS_ERROR.getErrorCode(), PeacockCKError.CK_NATIVE_LIBS_ERROR.getErrorMsg());
                            }
                            watcherInitList.clear();
                        } catch (Throwable ignored) {
                            CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "watcherInitList iterator.next().onError remove error.");
                        }
                    }
                }
            });
        }
    }

    private void tryFetchCKResources() {
        // 鉴权文件及模型文件下载
        if (ckResourcesManager.getCKFetchResourcesFlag() == CKResourcesManager.CKFetchResourcesFlag.RESOURCES_DOWNLOADED) {
            initCKSDK(mContext);
        } else {
            fetchCKResources(new CKResourcesManager.CKDownloadResourcesListener() {
                @Override
                public void onSuccess(String licenseFilePath, String modelFilePath) {
                    // 进行火山引擎初始化
                    CKHelper.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initCKSDK(mContext);
                        }
                    });
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    isPeacockSolutionInit.set(false);
                    isPeacockSolutionInitPreparing.set(false);

                    ckInitErrorCode = PeacockCKError.CK_RESOURCES_ERROR.getErrorCode();

                    synchronized (PeacockCKSolution.this) {
                        try {
                            ListIterator<CKInitListener> iterator = watcherInitList.listIterator();
                            while (iterator.hasNext()) {
                                iterator.next().onError(PeacockCKError.CK_RESOURCES_ERROR.getErrorCode(), PeacockCKError.CK_RESOURCES_ERROR.getErrorMsg());
                            }
                            watcherInitList.clear();
                        } catch (Throwable ignored) {
                            CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "watcherInitList iterator.next().onError remove error.");
                        }
                    }
                }
            });
        }
    }


    /**
     * 火山引擎初始化
     *
     * @param mContext  上下文
     */
    private void initCKSDK(Context mContext) {
        // 调试模式下开启火山引擎日志
        if (Const.isDebug) {
            LogWrapper logWrapper = new LogWrapper();
            LogConfig logConfig = new LogConfig.Builder()
                    .logcatLevel(LogIF.LOG_LEVER.DEBUG)
                    .localLevel(LogIF.LOG_LEVER.WARNING)
                    .toLocal(true)
                    .toLogcat(true)
                    .showThreadInfo(true)
                    .localPath(mContext.getCacheDir().getPath())
                    .build();
            logWrapper.init(logConfig);
            CutSameSolution.INSTANCE.setLogIf(logWrapper);
        }

        isPeacockSolutionInit.set(true);
        String audioAuthKey = "";
        String audioAuthToken = "";
        if (ckResourcesManager.getVeAccountInfos() != null && ckResourcesManager.getVeAccountInfos().getVe_android() != null) {
            audioAuthKey = ckResourcesManager.getVeAccountInfos().getVe_android().getVeAppKey();
        }
        if (ckResourcesManager.getVeAccountInfos() != null && ckResourcesManager.getVeAccountInfos().getVe_android() != null) {
            audioAuthToken = ckResourcesManager.getVeAccountInfos().getVe_android().getVeToken();
        }
        String host = Const.host;
        String effectLitPath = Const.effectLitPath;

        try {
            AuthorityConfig.AuthorityListener authorityListener = new AuthorityConfig.AuthorityListener() {
                @Override
                public void onError(int errorCode, String errorMsg) {
                    ckInitErrorCode = PeacockCKError.AUTHORITY_INVALID.getErrorCode();
                    authorCKSolutionError(PeacockCKError.AUTHORITY_INVALID.getErrorCode(), PeacockCKError.AUTHORITY_INVALID.getErrorMsg());
                }
            };
            AuthorityConfig authorityConfig = new AuthorityConfig.Builder()
                    .licensePath(ckResourcesManager.getLicenseResourcePath())
                    .audioAppKey(audioAuthKey)
                    .audioToken(audioAuthToken)
                    .authorityListener(authorityListener).build();

            TemplateFetcherConfig templateFetcherConfig = new TemplateFetcherConfig.Builder().host(Const.host).build();

            EffectFetcherConfig effectFetcherConfig = new EffectFetcherConfig.Builder()
                    .host(host)
                    .localModelPath(ckResourcesManager.getModelResourcePath())
                    .effectListFetcher(new EffectListFetcher())
                    .effectLitPath(effectLitPath)
                    .build();

            CKHelper.novaCodeLogI(PeacockCKSolution.class, TAG, "CutSameSolution.INSTANCE.init");

            // so 列表加载成功 及 资源文件下载成功后 => CK SDK 进行初始化
            if (ckNativeLibsManager.getCKFetchNativeLibsFlag() == CKNativeLibsManager.CKFetchNativeLibsFlag.SOS_LOADED &&
                    ckResourcesManager.getCKFetchResourcesFlag() == CKResourcesManager.CKFetchResourcesFlag.RESOURCES_DOWNLOADED) {
                ckNativeLibsManager.loadNativeLibsListeners();
                CutSameSolution.INSTANCE.init((Application) mContext, authorityConfig, templateFetcherConfig, effectFetcherConfig);
                UriAdapterSwitch.open = true;
                VEConfigCenter.getInstance().updateValue(VEConfigKeys.KEY_ENABLE_RENDER_ENCODE_RESOLUTION_ALIGN4, true);
                VEConfigCenter.getInstance().syncConfigToNative();
                if (isPeacockSolutionInit.get()) {
                    CKHelper.novaCodeLogI(PeacockCKSolution.class, TAG, "Authority success");

                    synchronized (PeacockCKSolution.this) {
                        try {
                            ListIterator<CKInitListener> iterator = watcherInitList.listIterator();
                            while (iterator.hasNext()) {
                                iterator.next().onSuccess();
                            }
                            watcherInitList.clear();
                        } catch (Throwable ignored) {
                            CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "watcherInitList iterator.next().onError remove error.");
                        }
                    }
                }
            } else {
                ckInitErrorCode = PeacockCKError.CK_NATIVE_LIBS_ERROR.getErrorCode();

                synchronized (PeacockCKSolution.this) {
                    try {
                        ListIterator<CKInitListener> iterator = watcherInitList.listIterator();
                        while (iterator.hasNext()) {
                            iterator.next().onError(PeacockCKError.CK_NATIVE_LIBS_ERROR.getErrorCode(), PeacockCKError.CK_NATIVE_LIBS_ERROR.getErrorMsg());
                        }
                        watcherInitList.clear();
                    } catch (Throwable ignored) {
                        CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "watcherInitList iterator.next().onError remove error.");
                    }
                }
            }
        } catch (Exception e) {
            ckInitErrorCode = PeacockCKError.CK_SOLUTION_INIT_ERROR.getErrorCode();
            authorCKSolutionError(PeacockCKError.CK_SOLUTION_INIT_ERROR.getErrorCode(), PeacockCKError.CK_SOLUTION_INIT_ERROR.getErrorMsg());
        }

        isPeacockSolutionInitPreparing.set(false);
    }


    /**
     * 火山引擎鉴权失败
     *
     * @param errorCode
     * @param errorMsg
     */
    private void authorCKSolutionError(int errorCode, String errorMsg) {
        CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "Authority errorCode=" + errorCode + " ;errorMsg : " + errorMsg);

        isPeacockSolutionInit.set(false);

        synchronized (PeacockCKSolution.this) {
            try {
                ListIterator<CKInitListener> iterator = watcherInitList.listIterator();
                while (iterator.hasNext()) {
                    iterator.next().onError(errorCode, errorMsg);
                }
                watcherInitList.clear();
            } catch (Throwable ignored) {
                CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "watcherInitList iterator.next().onError remove error.");
            }
        }
    }


    /**
     * 预下载: 下载 effects 素材和 template.zip
     *
     * @param mContext
     * @param cutSameModel
     * @param ckPrepareSourceListener 注册下载进度监听的回调
     */
    public void prepareTemplateSource(final Context mContext, final CutSameModel cutSameModel, final CKProgressDealSourceListener ckPrepareSourceListener) {
        if (this.mContext == null){
            this.mContext = mContext;
        }
        prepareTemplateSource(cutSameModel, ckPrepareSourceListener);
    }


    public void prepareTemplateSource(final CutSameModel cutSameModel, final CKProgressDealSourceListener ckPrepareSourceListener) {
        // 初始化失败时，重新进行引擎初始化
        if (!isPeacockSolutionInit.get()) {
            this.init(mContext, privacyToken, new CKInitListener() {
                @Override
                public void onSuccess() {
                    prepareSourceWithoutInitSDK(cutSameModel, ckPrepareSourceListener);
                }

                @Override
                public void onInitPreparing() {

                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    ckPrepareSourceListener.onError(errorCode, errorMsg);
                }
            });
            return;
        }

        prepareSourceWithoutInitSDK(cutSameModel, ckPrepareSourceListener);
    }


    private void prepareSourceWithoutInitSDK(CutSameModel cutSameModel, CKProgressDealSourceListener ckPrepareSourceListener) {
        CKTask task = this.getTaskOrNew(cutSameModel);

        // 避免重复 prepareSource
        if (task.isSourcePrepared()) {
            ckPrepareSourceListener.onSuccess();
            return;
        }

        if (ckPrepareSourceListener != null) {
            registerPrepareSourceListener(cutSameModel, ckPrepareSourceListener);
        }

        if (!task.isSourcePreparing()) {
            task.downloadEffectsAndPrePareSource(cutSameModel.getEffectJsonUrl());
        }
    }


    /**
     * 媒体素材处理: 下载解析后的素材需要调用该方法，素材才会有TargetStartTime & 倒放素材需要通过该方法实现效果
     *
     * @param cutSameModel
     * @param ckCompressSourceListener 注册处理进度监听的回调
     */
    public void compressTemplateSource(CutSameModel cutSameModel, CKProgressDealSourceListener ckCompressSourceListener) {
        if (!isPeacockSolutionInit.get()) {
            ckCompressSourceListener.onError(ckInitErrorCode, PeacockCKError.fromErrorCode(ckInitErrorCode).getErrorMsg());
            return;
        }

        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null) return;
        task.setCutSameModel(cutSameModel);

        // 避免重复 compressTemplateSource
        // 压缩处理后的文件是放到file路径 且 存储空间上限为1000MB；故进入模版首次调用 composeSource 后会可根据模版素材实例对应的id判断是否进行重复使用
        if (task.isSourceCompressed()) {
            ckCompressSourceListener.onSuccess();
            return;
        }

        ArrayList<VideoMaterial> videoMaterials = cutSameModel.getVideoMaterials();
        // 物料不存在时，则直接返回
        if (videoMaterials == null || videoMaterials.size() == 0 || !this.checkDataOkOrNot(videoMaterials)) {
            ckCompressSourceListener.onError(PeacockCKError.VIDEO_MATERIAL_NOT_EXIST_ERROR.getErrorCode(), PeacockCKError.VIDEO_MATERIAL_NOT_EXIST_ERROR.getErrorMsg());
            return;
        }

        ArrayList<MediaItem> mediaItems = DataHelper.convertVideoMaterials2MediaItems(videoMaterials, task.getMutableMediaItemList());
        if (mediaItems == null) return;

        if (ckCompressSourceListener != null) {
            registerCompressSourceListener(cutSameModel, ckCompressSourceListener);
        }

        task.composeSource(mediaItems);
    }


    /**
     * 预下载 && 媒体素材处理
     *
     * @param cutSameModel
     * @param prepareAndCompressTemplateSource
     */
    public void prepareAndCompressTemplateSource(final CutSameModel cutSameModel, final CKProgressDealSourceListener prepareAndCompressTemplateSource) {
        this.prepareTemplateSource(cutSameModel, new CKProgressDealSourceListener() {
            @Override
            public void onProgress(float progress) {
            }

            @Override
            public void onSuccess() {
                PeacockCKSolution.getInstance().compressTemplateSource(cutSameModel, new CKProgressDealSourceListener() {
                    @Override
                    public void onProgress(float progress) {
                    }

                    @Override
                    public void onSuccess() {
                        prepareAndCompressTemplateSource.onSuccess();
                    }

                    @Override
                    public void onError(int errorCode, @Nullable String errorMsg) {
                        // composeSource 错误码： 包括 VIDEO_MATERIAL_NOT_EXIST_ERROR 及 其他火山引擎定义的媒体素材处理失败的错误码
                        prepareAndCompressTemplateSource.onError(errorCode, errorMsg);
                    }
                });
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
                // prepareSource 错误码： 包括 PREPARE_EFFECT_JSON_ERROR 及 PREPARE_SOURCE_ERROR
                prepareAndCompressTemplateSource.onError(errorCode, errorMsg);
            }
        });
    }


    /**
     * 取消媒体素材处理
     *
     * @param cutSameModel
     * @param ckCompressSourceListener
     */
    public void cancelCompressTemplateSource(CutSameModel cutSameModel, CKProgressDealSourceListener ckCompressSourceListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null) return;

        if (task.getCutSameSource() != null) {
            task.getCutSameSource().cancelCompose();
        }

        unRegisterCompressSourceListener(cutSameModel, ckCompressSourceListener);
    }


    /**
     * 视频模版成品导出
     *
     * @param cutSameModel
     * @param outputVideoPath        导出成品存储路径
     * @param ckParam                导出配置参数
     * @param ckExportSourceListener
     */
    public void exportSource(final CutSameModel cutSameModel, final String outputVideoPath, final CKCompileParam ckParam, final CKProgressDealSourceListener ckExportSourceListener) {
        this.prepareAndCompressTemplateSource(cutSameModel, new CKProgressDealSourceListener() {
            @Override
            public void onProgress(float progress) {

            }

            @Override
            public void onSuccess() {
                PeacockCKSolution.getInstance().exportSourceWithoutPrepareSource(cutSameModel, outputVideoPath, ckParam, ckExportSourceListener);
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
                // prepareSource 错误码： 包括 PREPARE_EFFECT_JSON_ERROR 及 PREPARE_SOURCE_ERROR
                // composeSource 错误码： 包括 VIDEO_MATERIAL_NOT_EXIST_ERROR 及 其他火山引擎定义的媒体素材处理失败的错误码
                ckExportSourceListener.onError(errorCode, errorMsg);
            }
        });
    }


    public void exportSourceWithoutPrepareSource(CutSameModel cutSameModel, String outputVideoPath, CKCompileParam ckParam, CKProgressDealSourceListener ckExportSourceListener) {
        if (!isPeacockSolutionInit.get()) {
            ckExportSourceListener.onError(ckInitErrorCode, PeacockCKError.fromErrorCode(ckInitErrorCode).getErrorMsg());
            return;
        }

        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null) return;

        if (ckExportSourceListener != null) {
            registerExportSourceListener(cutSameModel, ckExportSourceListener);
        }

        CompileParam param = ckParam.convert2CompileParam();
        task.exportSource(mContext, outputVideoPath, param);
        if (mContext == null) {
            ckExportSourceListener.onError(ckInitErrorCode, PeacockCKError.fromErrorCode(ckInitErrorCode).getErrorMsg());
            return;
        }
        task.exportSource(mContext, outputVideoPath, param);
    }


    /**
     * 取消成品导出
     *
     * @param cutSameModel
     * @param ckExportSourceListener
     */
    public void cancelExportSource(CutSameModel cutSameModel, CKProgressDealSourceListener ckExportSourceListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null) return;

        String playerId = task.getCutSameModelId() + CKTask.ExportSourceMethodName;
        CutSamePlayer cutSamePlayer = task.getCutSamePlayer(playerId);
        if (cutSamePlayer != null) {
            cutSamePlayer.cancelCompile();
            task.releasePlayer(playerId);
        }

        unRegisterExportSourceListener(cutSameModel, ckExportSourceListener);
    }


    /**
     * 获取时间点的截图
     *
     * @param cutSameModel
     * @param timeStamps       要获取的时间点，单位毫秒
     * @param thumbnailWidth   要获取的帧宽度
     * @param thumbnailHeight  要获取的帧高度
     * @param getImageListener 取帧回调接口
     */
    public void getVideoFrameWithTime(final Context mContext, final CutSameModel cutSameModel, final int[] timeStamps, final int thumbnailWidth, final int thumbnailHeight, final CKGetImageListener getImageListener) {
        if (this.mContext == null){
            this.mContext = mContext;
        }
        getVideoFrameWithTime(cutSameModel, timeStamps, thumbnailWidth, thumbnailHeight, getImageListener);
    }


    public void getVideoFrameWithTime(final CutSameModel cutSameModel, final int[] timeStamps, final int thumbnailWidth, final int thumbnailHeight, final CKGetImageListener getImageListener) {
        this.prepareAndCompressTemplateSource(cutSameModel, new CKProgressDealSourceListener() {
            @Override
            public void onProgress(float progress) {

            }

            @Override
            public void onSuccess() {
                PeacockCKSolution.getInstance().getVideoFrameWithoutPrepareSource(cutSameModel, timeStamps, thumbnailWidth, thumbnailHeight, getImageListener);
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
                // prepareSource 错误码： 包括 PREPARE_EFFECT_JSON_ERROR 及 PREPARE_SOURCE_ERROR
                // composeSource 错误码： 包括 VIDEO_MATERIAL_NOT_EXIST_ERROR 及 其他火山引擎定义的媒体素材处理失败的错误码
                getImageListener.onError(errorCode, errorMsg);
            }
        });
    }


    public void getVideoFrameWithoutPrepareSource(CutSameModel cutSameModel, int[] timeStamps, int thumbnailWidth, int thumbnailHeight, CKGetImageListener getImageListener) {
        if (!isPeacockSolutionInit.get()) return;

        CKTask task = this.getTaskOrNew(cutSameModel);
        if (task == null) return;

        if (getImageListener != null) {
            registerGetViewFrameListener(cutSameModel, getImageListener);
        }

        if (mContext == null) {
            getImageListener.onError(ckInitErrorCode, PeacockCKError.fromErrorCode(ckInitErrorCode).getErrorMsg());
            return;
        }
        task.getVideoFrameWithTime(mContext, timeStamps, thumbnailWidth, thumbnailHeight);
    }


    /**
     * 取消获取时间点的截图
     *
     * @param cutSameModel
     * @param getImageListener
     */
    public void cancelGetVideoFrame(CutSameModel cutSameModel, CKGetImageListener getImageListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null) return;

        String playerId = task.getCutSameModelId() + CKTask.GetFrameMethodName;
        CutSamePlayer cutSamePlayer = task.getCutSamePlayer(playerId);
        if (cutSamePlayer != null) {
            cutSamePlayer.cancelGetVideoFrames();
            task.releasePlayer(playerId);
        }

        unRegisterGetViewFrameListener(cutSameModel, getImageListener);
    }


    /**
     * 检查媒体素材是否存在
     * @param videoMaterialList
     * @return
     */
    private boolean checkDataOkOrNot(List<VideoMaterial> videoMaterialList) {
        int missSourceCount = 0;
        for (VideoMaterial item : videoMaterialList) {
            if (!MediaUtils.isFileExists(item.getPath(), this.mContext, this.privacyToken)) {
                missSourceCount++;
            }
        }
        return missSourceCount == 0;
    }


    /**
     * 注册下载进度监听的回调
     */
    public void registerPrepareSourceListener(CutSameModel cutSameModel, CKProgressDealSourceListener ckPrepareSourceListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null || ckPrepareSourceListener == null) return;

        task.ckPrepareSourceListeners.registerListener(ckPrepareSourceListener);
    }


    /**
     * 注册媒体素材处理进度监听的回调
     */
    public void registerCompressSourceListener(CutSameModel cutSameModel, CKProgressDealSourceListener ckCompressSourceListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null || ckCompressSourceListener == null) return;

        task.ckCompressSourceListeners.registerListener(ckCompressSourceListener);
    }


    /**
     * 注册成品导出进度监听的回调
     */
    public void registerExportSourceListener(CutSameModel cutSameModel, CKProgressDealSourceListener ckExportSourceListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null || ckExportSourceListener == null) return;

        task.ckExportSourceListeners.registerListener(ckExportSourceListener);
    }


    /**
     * 注册获取时间点的截图的回调
     */
    public void registerGetViewFrameListener(CutSameModel cutSameModel, CKGetImageListener getImageListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null || getImageListener == null) return;

        task.getImageListeners.registerListener(getImageListener);
    }


    /**
     * 取消注册下载进度监听的回调
     */
    public void unRegisterPrepareSourceListener(CutSameModel cutSameModel, CKProgressDealSourceListener ckPrepareSourceListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null || ckPrepareSourceListener == null) return;

        task.ckPrepareSourceListeners.unRegisterListener(ckPrepareSourceListener);
    }


    /**
     * 取消注册媒体素材处理进度监听的回调
     */
    public void unRegisterCompressSourceListener(CutSameModel cutSameModel, CKProgressDealSourceListener ckCompressSourceListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null || ckCompressSourceListener == null) return;

        task.ckCompressSourceListeners.unRegisterListener(ckCompressSourceListener);
    }


    /**
     * 取消注册成品导出进度监听的回调
     */
    public void unRegisterExportSourceListener(CutSameModel cutSameModel, CKProgressDealSourceListener ckExportSourceListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null || ckExportSourceListener == null) return;

        task.ckExportSourceListeners.unRegisterListener(ckExportSourceListener);
    }


    /**
     * 取消注册获取时间点的截图的回调
     */
    public void unRegisterGetViewFrameListener(CutSameModel cutSameModel, CKGetImageListener getImageListener) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null || getImageListener == null) return;

        task.getImageListeners.unRegisterListener(getImageListener);
    }


    // -------------------- 任务管理相关 --------------------
    // 添加 task
    public void addTask(String taskId, CKTask task) {
        ckTaskManager.put(taskId, task);
    }

    // 获取 task
    public CKTask getTaskOrNull(CutSameModel cutSameModel) {
        return ckTaskManager.get(cutSameModel.getMd5());
    }

    // 获取 task，如果不存在则新建一个 task
    public CKTask getTaskOrNew(CutSameModel cutSameModel) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null) {
            task = new CKTask();
            task.setTaskId(cutSameModel.getMd5());
            task.setCutSameModel(cutSameModel);
            addTask(task.getTaskId(), task);
        }
        return task;
    }

    // 删除 task
    public void removeTask(CutSameModel cutSameModel) {
        ckTaskManager.remove(cutSameModel.getMd5());
    }

    /**
     * 释放所有模版对应的资源
     */
    public void releaseAllTasks() {
        for (CKTask task : ckTaskManager.values()) {
            releaseTask(task);
        }

        ckTaskManager.clear();
        watcherInitList.clear();
    }

    /**
     * 释放单一模版对应的资源
     */
    public void releaseTask(CutSameModel cutSameModel) {
        CKTask task = this.getTaskOrNull(cutSameModel);
        if (task == null) return;
        releaseTask(task);
    }


    private void releaseTask(CKTask task) {
        removeTask(task.getCutSameModel());
        task.release();
    }


    public interface CKProgressDealSourceListener {
        void onProgress(float progress);

        void onSuccess();

        void onError(int errorCode, @Nullable String errorMsg);
    }


    public interface CKGetImageListener {
        void frameBitmap(String timeStamp, Bitmap bitmap);

        void onError(int errorCode, String errorMsg);
    }


    /**
     * 引擎初始化回调
     */
    public interface CKInitListener {
        void onSuccess();

        /**
         * 正在进行初始化
         */
        void onInitPreparing();

        /**
         * 初始化错误回调：区分资源文件下载错误 及 鉴权错误
         *
         * @param errorCode: 鉴权文件及模型文件下载失败-CK_RESOURCES_ERROR_CODE; 鉴权失效-AUTHORITY_INVALID_CODE；引擎初始化失败 CK_SOLUTION_INIT_ERROR_CODE
         * @param errorMsg
         */
        void onError(int errorCode, String errorMsg);
    }

    public enum PeacockCKError {
        // -------------------- 初始化状态相关错误码定义 --------------------
        /**
         * 默认异常
         */
        IDLE_ERROR(-1103000, "default error."),
        /**
         * 鉴权失效错误码
         */
        AUTHORITY_INVALID(-1103001, "authority error."),
        /**
         * token 文件、鉴权文件及模型文件下载失败错误码
         */
        CK_RESOURCES_ERROR(-1103002, "DD download CK resources error."),
        /**
         * 火山引擎初始化失败错误码
         */
        CK_SOLUTION_INIT_ERROR(-1103003, "init ckSolution error."),

        // -------------------- effectJson及模版下载 & 物料合成 错误码定义 --------------------
        /**
         * effectJson下载错误码
         */
        PREPARE_EFFECT_JSON_ERROR(-1103004, "prepare effectJson error."),
        /**
         * 模版准备失败错误码：下载失败 || 其他准备过程中错误
         */
        PREPARE_SOURCE_ERROR(-1103005, "prepare source error."),
        /**
         * 素材失效错误码
         */
        VIDEO_MATERIAL_NOT_EXIST_ERROR(-1103006, "some videoMaterials do not exit."),
        /**
         * so 库列表加载失败错误码
         */
        CK_NATIVE_LIBS_ERROR(-1103007, "load so native libs error."),
        /**
         * 创建 player 失败
         */
        CK_CREATE_PLAYER_ERROR(-1103008, "create player error.");


        public static PeacockCKError fromErrorCode(int errorCode) {
            for (PeacockCKError error : PeacockCKError.values()) {
                if (error.getErrorCode() == errorCode) {
                    return error;
                }
            }
            return IDLE_ERROR;
        }

        private final int errorCode;
        private final String errorMsg;

        PeacockCKError(int errorCode, String errorMsg) {
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

}
