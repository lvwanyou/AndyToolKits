package com.dianping.video;

import android.support.annotation.Nullable;

import com.dianping.codelog.NovaCodeLog;
import com.dianping.util.FileUtils;
import com.dianping.util.ZipUtils;
import com.dianping.video.bean.VeAccountInfos;
import com.dianping.video.utils.CKHelper;
import com.google.gson.Gson;
import com.meituan.met.mercury.load.core.DDLoadParams;
import com.meituan.met.mercury.load.core.DDLoadStrategy;
import com.meituan.met.mercury.load.core.DDLoader;
import com.meituan.met.mercury.load.core.DDLoaderManager;
import com.meituan.met.mercury.load.core.DDResource;
import com.meituan.met.mercury.load.core.LoadCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class CKResourcesManager {
    private static final String TAG = "CK-CKResourcesManager";

    // -------------------- 下载资源相关 --------------------
    private static final String CK_LICENSE_MODEL_RESOURCE_BUSINESS = "peacock";
    private static final String CK_LICENSE_MODEL_RESOURCE_NAME = "peacock_novapeacockresources";

    /**
     * 判断资源是否下载完成了
     */
    private boolean isResourceDownloaded = false;
    /**
     * 判断资源是否处于下载中...
     */
    private boolean isDownloadingSource = false;

    /**
     * 用来保存用于引擎初始化的 token 资源的路径
     */
    private VeAccountInfos veAccountInfos = null;

    public VeAccountInfos getVeAccountInfos() {
        return veAccountInfos;
    }

    /**
     * 用来保存模型资源的路径
     */
    private String modelResourcePath = "";

    public String getModelResourcePath() {
        return modelResourcePath;
    }

    /**
     * 用来保存鉴权文件的路径
     */
    private String licenseResourcePath = "";

    public String getLicenseResourcePath() {
        return licenseResourcePath;
    }

    /**
     * 用来保存资源下载的所有监听器
     */
    private final ArrayList<CKDownloadResourcesListener> watcherList = new ArrayList();


    /**
     * 解压缩文件
     *
     * @param zipPath 压缩文件路径
     */
    private String unZipPackage(String zipPath) {
        // 获取Zip文件
        File zipFile = new File(zipPath);
        // 如果找不到文件，返回无效路径
        if (!zipFile.exists()) {
            return zipPath;
        }
        // 如果存在则开始进行资源包的解压缩
        File unZipDir = zipFile.getParentFile(); // 解压缩后的ck资源文件还放在原zip文件的父目录下
        try {
            if (unZipDir != null) {
                ZipUtils.unzipFile(zipFile, unZipDir);
            }
        } catch (IOException e) {
            NovaCodeLog.e(CKResourcesManager.class, "unZip file " + zipFile.getName() + " failed!!");
        }
        return unZipDir != null ? unZipDir.getAbsolutePath() : "";
    }


    /**
     * 注册资源下载监听器
     *
     * @param watcher 监听器
     */
    void registerResourceWatcher(CKDownloadResourcesListener watcher) {
        if (watcher != null) {
            if (!watcherList.contains(watcher)) {
                watcherList.add(watcher);
            }
        }
    }


    /**
     * 解注册资源下载监听器
     *
     * @param watcher 监听器
     */
    void unRegisterResourceWatcher(CKDownloadResourcesListener watcher) {
        if (watcher != null) {
            watcherList.remove(watcher);
        }
    }


    /**
     * <p>
     * 进行鉴权文件+算法模型文件下载
     * <p>
     * 详细说明：使用 <a href="https://km.sankuai.com/page/382116918">DDD</a> 进行资源下载
     */
    public void downLoadCKResources(CKDownloadResourcesListener ckDownloadResourcesListener) {
        // 避免重复下载
        if (isDownloadingSource) {
            return;
        }

        if (ckDownloadResourcesListener != null) {
            registerResourceWatcher(ckDownloadResourcesListener);
        }

        if (isResourceDownloaded) {
            tryCallbackDownloadListener();
            return;
        }

        DDLoader ckResourcesLoader = DDLoaderManager.getLoader(CK_LICENSE_MODEL_RESOURCE_BUSINESS);

        // 使用测试环境
//        ckResourcesLoader.setTestEnv(true);

        DDLoadParams ddLoadParams = new DDLoadParams(DDLoadParams.STORAGE_MODE_FILE);   // 资源文件存储在file目录下

        isDownloadingSource = true;
        isResourceDownloaded = false;

        // 批量获取最新部署的资源; 会被多次回调，每个资源回调一次
        ckResourcesLoader.loadResource(
                CK_LICENSE_MODEL_RESOURCE_NAME,
                DDLoadStrategy.NET_FIRST,
                ddLoadParams,
                new LoadCallback() {
                    @Override
                    public void onSuccess(@Nullable DDResource ddResource) {
                        CKHelper.novaCodeLogI(CKResourcesManager.class, TAG, "DD download CK resources success.");
                        CKHelper.novaCodeLogI(CKResourcesManager.class, TAG, "DD download CK resources version: " + ddResource.getVersion());
                        CKHelper.novaCodeLogI(CKResourcesManager.class, TAG, "DD download CK resources localPath: " + ddResource.getLocalPath());

                        String parentPath = unZipPackage(ddResource.getLocalPath());
                        File[] fileArray = new File(parentPath).listFiles();

                        if (fileArray == null) {
                            isResourceDownloaded = false;
                            tryCallbackDownloadListener();
                            return;
                        }

                        for (File file : fileArray) {
                            // 获取用于引擎初始化的 token 的 json 文件
                            if (file.getName().contains("veAccountInfos.json")) {
                                try {
                                    String jsonString = FileUtils.getString(file);
                                    veAccountInfos = new Gson().fromJson(jsonString, VeAccountInfos.class);
                                } catch (Exception e) {
                                    CKHelper.novaCodeLogE(CKResourcesManager.class, TAG, "jsonString transform veAccountInfos error.");

                                    isResourceDownloaded = false;
                                    // 下载失败立马给出资源监听回调
                                    tryCallbackDownloadListener();
                                }
                            }
                            // 获取模型文件路径
                            if (file.getName().endsWith(".bundle")) {
                                modelResourcePath = file.getAbsolutePath();
                            }
                            // 获取鉴权文件路径
                            if (file.getName().contains("dianping_test_com.dianping.v1_cutsame")) {
                                licenseResourcePath = file.getAbsolutePath();
                            }
                        }

                        isResourceDownloaded = true;
                        tryCallbackDownloadListener();
                    }

                    @Override
                    public void onFail(Exception e) {
                        CKHelper.novaCodeLogE(CKResourcesManager.class, TAG, "DD download CK resources error.");

                        isResourceDownloaded = false;
                        // 下载失败立马给出资源监听回调
                        tryCallbackDownloadListener();
                    }
                });
    }


    /**
     * 尝试回调下载回调，并且将注册的回调取消注册
     */
    private void tryCallbackDownloadListener() {
        // 资源下载结束回调
        isDownloadingSource = false;

        synchronized (watcherList) {
            for (Iterator<CKDownloadResourcesListener> iterator = watcherList.iterator(); iterator.hasNext(); ) {
                if (isResourceDownloaded) {
                    iterator.next().onSuccess(licenseResourcePath, modelResourcePath);
                } else {
                    iterator.next().onError(PeacockCKSolution.PeacockCKError.CK_RESOURCES_ERROR.getErrorCode(), PeacockCKSolution.PeacockCKError.CK_RESOURCES_ERROR.getErrorMsg());
                }
                iterator.remove();
            }
        }
    }


    /**
     * 获取下载资源的标识
     *
     * @return 下载状态标识
     */
    public CKFetchResourcesFlag getCKFetchResourcesFlag() {
        if (isDownloadingSource) {
            return CKFetchResourcesFlag.RESOURCES_DOWNLOADING;
        } else if (isResourceDownloaded) {
            return CKFetchResourcesFlag.RESOURCES_DOWNLOADED;
        } else {
            return CKFetchResourcesFlag.RESOURCES_NOT_DOWNLOAD;
        }
    }


    public interface CKDownloadResourcesListener {
        void onSuccess(String licenseFilePath, String modelFilePath);

        void onError(int errorCode, String errorMsg);
    }

    public enum CKFetchResourcesFlag {
        RESOURCES_NOT_DOWNLOAD,
        RESOURCES_DOWNLOADING,
        RESOURCES_DOWNLOADED,
    }
}
