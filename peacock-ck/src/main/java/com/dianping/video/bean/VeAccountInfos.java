package com.dianping.video.bean;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * 详细说明：鉴权账号 json 对应的 bean 结构
 *
 * @author lvwanyou
 */
@Keep
public class VeAccountInfos {
    @SerializedName("ve_android")
    private VeAndroidDTO ve_android;

    @SerializedName("ve_ios")
    private VeIosDTO ve_ios;

    public VeAndroidDTO getVe_android() {
        return ve_android;
    }

    public VeIosDTO getVe_ios() {
        return ve_ios;
    }

    @Keep
    public static class VeAndroidDTO {
        @SerializedName("veAppKey")
        private String veAppKey;

        @SerializedName("veToken")
        private String veToken;

        public String getVeAppKey() {
            return veAppKey;
        }

        public String getVeToken() {
            return veToken;
        }
    }

    @Keep
    public static class VeIosDTO {
        @SerializedName("veAppKey")
        private String veAppKey;

        @SerializedName("veToken")
        private String veToken;

        public String getVeAppKey() {
            return veAppKey;
        }

        public String getVeToken() {
            return veToken;
        }
    }
}
