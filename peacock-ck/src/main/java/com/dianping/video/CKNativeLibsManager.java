package com.dianping.video;


import com.bytedance.ies.cutsame.cut_android.ITemplateNativeLibsLoader;
import com.bytedance.ies.cutsame.cut_android.TemplateSDK;
import com.dianping.video.utils.CKHelper;
import com.meituan.android.loader.DynLoader;
import com.meituan.android.loader.DynLoaderDownloadCallBack;
import com.meituan.android.loader.DynParams;
import com.ss.android.ttve.nativePort.TENativeLibsLoader;
import com.ss.ugc.effectplatform.algorithm.AlgorithmLibraryLoader;
import com.ss.ugc.effectplatform.algorithm.ILibraryLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class CKNativeLibsManager {
    private static final String TAG = "CK-CKNativeLibsManager";

    // -------------------- 通过 dynloader 加载的 so 库列表，需按以下顺序加载 --------------------
    private final String[] ckNativeLibs = {"c++_shared", "bytevc0", "ByteVC1_dec", "ttffmpeg", "yuv", "bdaudioeffect", "gaia_lib", "AGFX", "effect", "ttvesdk",
            "NLEEditorJni", "NLEMediaJni", "NLEMediaPublicJni", "NLETemplateModelJni",
            "CutSameConsumerJni", "CutSameJni", "newep"};

    /**
     * 判断 so 库是否处于下载中...
     */
    private final AtomicBoolean isNativeLibsDownloading = new AtomicBoolean(false);
    /**
     * 判断 so 库是否可用
     */
    private final AtomicBoolean isNativeLibsAvail = new AtomicBoolean(false);
    /**
     * 判断 so 库是否加载成功
     */
    private final AtomicBoolean isNativeLibsLoaded = new AtomicBoolean(false);


    /**
     * 用来保存加载 so 库的所有监听器
     */
    private final CopyOnWriteArrayList<CKLoadNativeLibsListener> watcherList = new CopyOnWriteArrayList<>();

    private final CopyOnWriteArrayList<CKDownloadNativeLibsListener> watcherDownloadLibsList = new CopyOnWriteArrayList<>();

    public CKNativeLibsManager() {
    }


    /**
     * 下载单个 so 资源
     *
     * @param libName                     so 库名称
     * @param cKDownloadNativeLibListener 下载回调
     */
    private void downloadCKNativeLib(final String libName, final CKDownloadNativeLibListener cKDownloadNativeLibListener) {
        if (DynLoaderAvailable(libName)) {
            if (cKDownloadNativeLibListener != null) {
                cKDownloadNativeLibListener.onSuccess();
            }
            return;
        }
        // 未加载成功的 so 库列表
        List<String> unAvailableSo = new ArrayList<>();
        unAvailableSo.add(libName);
        DynParams dynParams = new DynParams.DynParamsBuilder()
                .withSoFileParams(unAvailableSo)
                .build();
        DynLoader.toggleDownload(new DynLoaderDownloadCallBack() {
            @Override
            public void onDynDownloadSuccess() {
                CKHelper.novaCodeLogI(CKNativeLibsManager.class, TAG, "DynLoader single so toggleDownload SUCCESS: " + libName);

                if (cKDownloadNativeLibListener != null) {
                    cKDownloadNativeLibListener.onSuccess();
                }
            }

            @Override
            public void onDynDownloadFailure() {
                CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "DynLoader single so toggleDownload FAIL: " + libName);

                if (cKDownloadNativeLibListener != null) {
                    cKDownloadNativeLibListener.onError(CKNativeLibsError.CK_SO_AVAILABLE_INVALID.errorCode, CKNativeLibsError.CK_SO_AVAILABLE_INVALID.errorMsg);
                }
            }
        }, dynParams, true);
    }


    /**
     * 下载多个 so 资源， 下载列表来源 {@link CKNativeLibsManager#ckNativeLibs}
     *
     * @param cKDownloadNativeLibsListener 下载回调
     */
    private void downloadCKNativeLibs(final CKDownloadNativeLibsListener cKDownloadNativeLibsListener) {
        if (!watcherDownloadLibsList.contains(cKDownloadNativeLibsListener)) {
            watcherDownloadLibsList.add(cKDownloadNativeLibsListener);
        }

        // 避免重复下载
        if (isNativeLibsDownloading.get()) {
            if (cKDownloadNativeLibsListener != null) {
                cKDownloadNativeLibsListener.onDownloading();
            }
            return;
        }

        // 未加载成功的 so 库列表
        List<String> unAvailableList = new ArrayList<>();
        for (String libName : ckNativeLibs) {
            if (!DynLoaderAvailable(libName)) {
                unAvailableList.add(libName);
            }
        }
        // 避免重复等待下载
        if (unAvailableList.size() == 0) {
            isNativeLibsDownloading.set(false);
            isNativeLibsAvail.set(true);

            synchronized (CKNativeLibsManager.this) {
                try {
                    ListIterator<CKDownloadNativeLibsListener> iterator = watcherDownloadLibsList.listIterator();
                    while (iterator.hasNext()) {
                        iterator.next().onSuccess();
                    }
                    watcherDownloadLibsList.clear();
                } catch (Throwable ignored) {
                    CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "watcherDownloadLibsList iterator.next() remove error.");
                }
            }
            return;
        }

        isNativeLibsDownloading.set(true);
        DynParams dynParams = new DynParams.DynParamsBuilder()
                .withSoFileParams(unAvailableList)
                .build();
        DynLoader.toggleDownload(new DynLoaderDownloadCallBack() {
            @Override
            public void onDynDownloadSuccess() {
                CKHelper.novaCodeLogI(CKNativeLibsManager.class, TAG, "DynLoader toggleDownload arrays SUCCESS.");

                isNativeLibsDownloading.set(false);
                isNativeLibsAvail.set(true);

                synchronized (CKNativeLibsManager.this) {
                    try {
                        ListIterator<CKDownloadNativeLibsListener> iterator = watcherDownloadLibsList.listIterator();
                        while (iterator.hasNext()) {
                            iterator.next().onSuccess();
                        }
                        watcherDownloadLibsList.clear();
                    } catch (Throwable ignored) {
                        CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "watcherDownloadLibsList iterator.next() remove error.");
                    }
                }
            }

            @Override
            public void onDynDownloadFailure() {
                CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "DynLoader toggleDownload arrays FAIL.");

                isNativeLibsDownloading.set(false);
                isNativeLibsAvail.set(false);
                isNativeLibsLoaded.set(false);

                synchronized (CKNativeLibsManager.this) {
                    try {
                        ListIterator<CKDownloadNativeLibsListener> iterator = watcherDownloadLibsList.listIterator();
                        while (iterator.hasNext()) {
                            iterator.next().onError(CKNativeLibsError.CK_SOS_AVAILABLE_INVALID.errorCode, CKNativeLibsError.CK_SOS_AVAILABLE_INVALID.errorMsg);
                        }
                        watcherDownloadLibsList.clear();
                    } catch (Throwable ignored) {
                        CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "watcherDownloadLibsList iterator.next() remove error.");
                    }
                }
            }
        }, dynParams, true);
    }


    /**
     * 加载 so 库
     *
     * @param libName                 so 库名称
     * @param cKLoadNativeLibListener 加载 so 库回调
     * @return 加载 so 库是否成功
     */
    private boolean loadNativeLib(String libName, final CKLoadNativeLibListener cKLoadNativeLibListener) {
        boolean isLoaded = false;
        if (DynLoaderAvailable(libName)) {
            isLoaded = DynLoaderLoad(libName);
            if (isLoaded) {
                CKHelper.novaCodeLogI(CKNativeLibsManager.class, TAG, "DynLoader load SUCCESS :" + libName);

                if (cKLoadNativeLibListener != null) {
                    cKLoadNativeLibListener.onSuccess(libName);
                }
            } else {
                CKHelper.novaCodeLogI(CKNativeLibsManager.class, TAG, "DynLoader load FAIL :" + libName);

                if (cKLoadNativeLibListener != null) {
                    cKLoadNativeLibListener.onError(CKNativeLibsError.CK_SO_LOAD_ERROR.errorCode, CKNativeLibsError.CK_SO_LOAD_ERROR.errorMsg);
                }
            }
        } else {
            cKLoadNativeLibListener.onError(CKNativeLibsError.CK_SO_AVAILABLE_INVALID.errorCode, CKNativeLibsError.CK_SO_AVAILABLE_INVALID.errorMsg);
        }
        return isLoaded;
    }


    /**
     * 加载 so 库列表
     *
     * @param cKLoadNativeLibsListener 加载 so 库回调
     */
    public void loadNativeLibs(final CKLoadNativeLibsListener cKLoadNativeLibsListener) {
        CKHelper.novaCodeLogI(CKNativeLibsManager.class, TAG, "CKNativeLibsManager loadNativeLibs()");

        if (cKLoadNativeLibsListener != null) {
            registerResourceWatcher(cKLoadNativeLibsListener);
        }

        if (isNativeLibsLoaded.get()) {
            isNativeLibsDownloading.set(false);
            isNativeLibsAvail.set(true);
            tryCallbackLoadLibsListener(CKFetchNativeLibsFlag.SOS_LOADED);
            return;
        }

        isNativeLibsLoaded.set(false);
        downloadCKNativeLibs(new CKDownloadNativeLibsListener() {
            @Override
            public void onDownloading() {
                CKHelper.novaCodeLogI(CKNativeLibsManager.class, TAG, "CKNativeLibsManager loadNativeLibs() onDownloading.");
            }

            @Override
            public void onSuccess() {
                isNativeLibsDownloading.set(false);

                boolean shouldContinue = true;
                for (int i = 0; i < ckNativeLibs.length && shouldContinue; i++) {
                    // 该方法为同步执行
                    shouldContinue = loadNativeLib(ckNativeLibs[i], new CKLoadNativeLibListener() {
                        @Override
                        public void onSuccess(String libName) {

                        }

                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            isNativeLibsAvail.set(false);
                            isNativeLibsLoaded.set(false);

                            tryCallbackLoadLibsListener(CKFetchNativeLibsFlag.SOS_LOADED_ERROR);
                        }
                    });
                }

                // 循环执行结束后设置加载标识状态
                isNativeLibsLoaded.set(shouldContinue);
                if (isNativeLibsLoaded.get()) {
                    tryCallbackLoadLibsListener(CKFetchNativeLibsFlag.SOS_LOADED);
                }
                CKHelper.novaCodeLogI(CKNativeLibsManager.class, TAG, "CKNativeLibsManager loadNativeLibs() finish and isNativeLibsLoaded: " + isNativeLibsLoaded.get());
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "CKNativeLibsManager loadNativeLibs() error and isNativeLibsLoaded: " + isNativeLibsLoaded.get());
                isNativeLibsDownloading.set(false);
                isNativeLibsAvail.set(false);
                isNativeLibsLoaded.set(false);

                tryCallbackLoadLibsListener(CKFetchNativeLibsFlag.SOS_AVAILABLE_INVALID);
            }
        });
    }


    /**
     * 尝试回调加载 so 库列表函数 {@link CKNativeLibsManager#loadNativeLibs(CKLoadNativeLibsListener)} 中的回调，并且将注册的回调取消注册
     *
     * @param ckFetchNativeLibsFlag 回调标识
     */
    private void tryCallbackLoadLibsListener(final CKFetchNativeLibsFlag ckFetchNativeLibsFlag) {
        CKHelper.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (CKNativeLibsManager.this) {
                    try {
                        ListIterator<CKLoadNativeLibsListener> iterator = watcherList.listIterator();
                        while (iterator.hasNext()) {
                            switch (ckFetchNativeLibsFlag) {
                                case SOS_LOADED:
                                    iterator.next().onSuccess();
                                    break;
                                case SOS_LOADED_ERROR:
                                    iterator.next().onError(CKNativeLibsError.CK_SOS_LOAD_ERROR.errorCode, CKNativeLibsError.CK_SOS_LOAD_ERROR.errorMsg);
                                    break;
                                case SOS_AVAILABLE_INVALID:
                                    iterator.next().onError(CKNativeLibsError.CK_SOS_AVAILABLE_INVALID.errorCode, CKNativeLibsError.CK_SOS_AVAILABLE_INVALID.errorMsg);
                                    break;
                                default:
                                    iterator.next().onError(CKNativeLibsError.IDLE_ERROR.errorCode, CKNativeLibsError.IDLE_ERROR.errorMsg);
                            }
                        }
                        watcherList.clear();
                    } catch (Throwable ignored) {
                        CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "CKLoadNativeLibsListener iterator.next() remove error.");
                    }
                }
            }
        });
    }


    /**
     * 注册so加载监听器
     *
     * @param watcher 监听器
     */
    void registerResourceWatcher(CKLoadNativeLibsListener watcher) {
        if (watcher != null) {
            if (!watcherList.contains(watcher)) {
                watcherList.add(watcher);
            }
        }
    }


    /**
     * 解注册so加载监听器
     *
     * @param watcher 监听器
     */
    void unRegisterResourceWatcher(CKLoadNativeLibsListener watcher) {
        if (watcher != null) {
            watcherList.remove(watcher);
        }
    }


    public interface CKDownloadNativeLibListener {
        void onSuccess();

        void onError(int errorCode, String errorMsg);
    }

    public interface CKLoadNativeLibListener {
        void onSuccess(String libName);

        void onError(int errorCode, String errorMsg);
    }

    public interface CKDownloadNativeLibsListener {
        /**
         * 正在进行下载
         */
        void onDownloading();

        void onSuccess();

        void onError(int errorCode, String errorMsg);
    }

    public interface CKLoadNativeLibsListener {
        void onSuccess();

        void onError(int errorCode, String errorMsg);
    }


    /**
     * 获取加载 so 库列表的标识
     *
     * @return 加载 so 库列表状态标识
     */
    public CKFetchNativeLibsFlag getCKFetchNativeLibsFlag() {
        if (isNativeLibsDownloading.get()) {
            return CKFetchNativeLibsFlag.SOS_DOWNLOADING;
        } else if (isNativeLibsAvail.get() && !isNativeLibsLoaded.get()) {
            return CKFetchNativeLibsFlag.SOS_AVAILABLE;
        } else if (isNativeLibsLoaded.get()) {
            return CKFetchNativeLibsFlag.SOS_LOADED;
        } else {
            return CKFetchNativeLibsFlag.SOS_NOT_DOWNLOAD;
        }
    }


    public enum CKFetchNativeLibsFlag {
        // so 列表相关
        SOS_NOT_DOWNLOAD,
        SOS_DOWNLOADING,
        SOS_AVAILABLE,
        SOS_AVAILABLE_INVALID,
        SOS_LOADED,
        SOS_LOADED_ERROR,

        // 单个 so 相关
        SO_AVAILABLE_INVALID,
        SO_LOADED_ERROR
    }


    public enum CKNativeLibsError {
        // -------------------- 相关错误码定义 --------------------
        /**
         * 默认异常
         */
        IDLE_ERROR(-1104101, "default error."),
        /**
         * so 库可用状态异常错误码
         */
        CK_SO_AVAILABLE_INVALID(-1104102, "so available invalid."),
        /**
         * so 库加载失败错误码
         */
        CK_SO_LOAD_ERROR(-1104103, "so load error."),
        /**
         * so 库列表可用状态异常错误码
         */
        CK_SOS_AVAILABLE_INVALID(-1104104, "sos available invalid."),
        /**
         * so 库列表加载失败错误码
         */
        CK_SOS_LOAD_ERROR(-1104105, "sos load error.");

        public static CKNativeLibsError fromErrorCode(int errorCode) {
            for (CKNativeLibsManager.CKNativeLibsError error : CKNativeLibsManager.CKNativeLibsError.values()) {
                if (error.getErrorCode() == errorCode) {
                    return error;
                }
            }
            return IDLE_ERROR;
        }

        private final int errorCode;
        private final String errorMsg;

        CKNativeLibsError(int errorCode, String errorMsg) {
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


    public void loadNativeLibsListeners() {
        // VESDK相关
        ExternalLibraryLoader externalLibraryLoader = new ExternalLibraryLoader(new TENativeLibsLoader.DefaultLibraryLoader());
        TENativeLibsLoader.setLibraryLoad(externalLibraryLoader);

        //加载 NLE 相关 so 库和 CutSameIF 相关 so 库
        TemplateSDK.libsLoader = new ITemplateNativeLibsLoader() {
            @Override
            public void loadLib(List<String> libsList) {
                //根据 so 名称加载对应 so
                try {
                    if (libsList != null) {
                        for (int i = 0; i < libsList.size(); i++) {
                            CKHelper.novaCodeLogI(PeacockCKSolution.class, TAG, "loadNativeLib : " + libsList.get(i));

                            if (!DynLoaderLoad(libsList.get(i))) {
                                isNativeLibsLoaded.set(false);
                                break;
                            }
                        }
                    }
                } catch (Throwable ignored) {
                    CKHelper.novaCodeLogE(PeacockCKSolution.class, TAG, "TemplateSDK.libsLoader loadNativeLib error.");

                    isNativeLibsLoaded.set(false);
                }

                //按序加载 libnewep.so
                try {
                    CKHelper.novaCodeLogI(PeacockCKSolution.class, TAG, "PeacockCKSolution loadNativeLib : newep");

                    isNativeLibsLoaded.set(isNativeLibsLoaded.get() && DynLoaderLoad("newep"));
                } catch (Throwable ignored) {
                    CKHelper.novaCodeLogI(PeacockCKSolution.class, TAG, "TemplateSDK.libsLoader loadNativeLib newep error.");

                    isNativeLibsLoaded.set(false);
                }
            }
        };

        // 加载 libnewep.so
        AlgorithmLibraryLoader.libraryLoader = new ILibraryLoader() {
            @Override
            public void loadLibrary(String libraryName) {
                //根据so名称加载对应so
                try {
                    CKHelper.novaCodeLogI(CKNativeLibsManager.class, TAG, "loadAlgorithmLibs - non-sequential: " + libraryName);

                    isNativeLibsLoaded.set(isNativeLibsLoaded.get() && DynLoaderLoad(libraryName));
                } catch (Throwable ignored) {
                    CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "AlgorithmLibraryLoader.libraryLoader non-sequential loadNativeLib error.");

                    isNativeLibsLoaded.set(false);
                }
            }
        };
    }


    public static boolean DynLoaderLoad(String libName) {
        try {
            return DynLoader.load(libName);
        } catch (Throwable ignored) {
            CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "check load error.");
        }
        return false;
    }


    public static boolean DynLoaderAvailable(String libName) {
        try {
            return DynLoader.available(libName, DynLoader.Type_LIB);
        } catch (Throwable ignored) {
            CKHelper.novaCodeLogE(CKNativeLibsManager.class, TAG, "check available error.");
        }
        return false;
    }
}
