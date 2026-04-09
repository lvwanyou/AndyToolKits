package com.dianping.video;

import com.dianping.video.utils.CKHelper;
import com.ss.android.ttve.nativePort.TENativeLibsLoader;

import java.util.List;

/**
 * 注册从外部加载so文件的回调
 *
 * @author lvwanyou
 */
public final class ExternalLibraryLoader implements TENativeLibsLoader.ILibraryLoader {
    private final TENativeLibsLoader.ILibraryLoader wrappedLibraryLoader;

    public ExternalLibraryLoader(TENativeLibsLoader.ILibraryLoader wrappedLibraryLoader) {
        this.wrappedLibraryLoader = wrappedLibraryLoader;
    }

    @Override
    public boolean onLoadNativeLibs(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (!CKNativeLibsManager.DynLoaderLoad(list.get(i))) {
                return false;
            }
        }
        return true;
    }
}