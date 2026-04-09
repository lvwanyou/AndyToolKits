package com.dianping.video.model;

import com.cutsame.solution.compile.CompileParam;
import com.cutsame.solution.compile.ExportResolution;
import com.ss.android.vesdk.VEVideoEncodeSettings;

/**
 * 导出配置参数
 *
 * @author lvwanyou
 */
public class CKCompileParam {
    public enum CKExportResolution {
        V_4K,
        V_2K,
        V_1080P,
        V_720P,
        V_480P
    }

    // 软编码相关参数，硬编码失败走软编码需要用到这些参数
    private static final long DEFAULT_SW_MAX_RATE = 1024 * 1024 * 30L;
    private static final int DEFAULT_SW_CRF = 21;

    private boolean supportHwEncoder;
    private int bps;
    private int fps;
    private int gopSize;

    public void setResolution(CKExportResolution resolution) {
        this.resolution = resolution;
    }

    private CKExportResolution resolution;

    public CKCompileParam(CKExportResolution resolution, boolean supportHwEncoder, int bps, int fps, int gopSize) {
        this.resolution = resolution;
        this.supportHwEncoder = supportHwEncoder;
        this.bps = bps;
        this.fps = fps;
        this.gopSize = gopSize;
    }

    public boolean isSupportHwEncoder() {
        return supportHwEncoder;
    }

    public void setSupportHwEncoder(boolean supportHwEncoder) {
        this.supportHwEncoder = supportHwEncoder;
    }

    public int getBps() {
        return bps;
    }

    public void setBps(int bps) {
        this.bps = bps;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getGopSize() {
        return gopSize;
    }

    public void setGopSize(int gopSize) {
        this.gopSize = gopSize;
    }

    public CKExportResolution getResolution() {
        return resolution;
    }

    public CompileParam convert2CompileParam() {
        ExportResolution resolution;
        switch (this.getResolution()) {
            case V_4K:
                resolution = ExportResolution.V_4K;
                break;
            case V_2K:
                resolution = ExportResolution.V_2K;
                break;
            case V_720P:
                resolution = ExportResolution.V_720P;
                break;
            case V_480P:
                resolution = ExportResolution.V_480P;
                break;
            default:
                resolution = ExportResolution.V_1080P;
                break;
        }
        return new CompileParam(resolution,
                this.isSupportHwEncoder(),
                this.getBps(),
                this.getFps(),
                this.getGopSize(),
                DEFAULT_SW_MAX_RATE,
                DEFAULT_SW_CRF,
                VEVideoEncodeSettings.ENCODE_BITRATE_MODE.ENCODE_BITRATE_CRF,
                VEVideoEncodeSettings.ENCODE_STANDARD.ENCODE_STANDARD_H264
        );
    }
}
