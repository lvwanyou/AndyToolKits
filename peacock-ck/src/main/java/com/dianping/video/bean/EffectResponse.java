package com.dianping.video.bean;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 详细说明：解析素材列表 json 后获取到的 response，包括素材文件的下载链接
 *
 * @author lvwanyou
 */
@Keep
public class EffectResponse {

    @SerializedName("ret")
    private Integer ret;

    @SerializedName("errmsg")
    private String errmsg;

    @SerializedName("data")
    private DataDTO data;

    public EffectResponse() {
        ret = 0;
        errmsg = "success";
        data = new DataDTO();
    }

    public Integer getRet() {
        return ret;
    }

    public void setRet(Integer ret) {
        this.ret = ret;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    @Keep
    public static class DataDTO {

        @SerializedName("effect_list")
        private List<EffectListDTO> effect_list;

        public DataDTO() {
            effect_list = new ArrayList<>();
        }

        public List<EffectListDTO> getEffect_list() {
            return effect_list;
        }

        public void setEffect_list(List<EffectListDTO> effect_list) {
            this.effect_list = effect_list;
        }

        @Keep
        public static class EffectListDTO {
            @SerializedName("id")
            private String id;

            @SerializedName("resource_id")
            private String resource_id;
            @SerializedName("name")
            private String name;
            @SerializedName("uri")
            private String uri;
            @SerializedName("url_list")
            private List<String> url_list;
            @SerializedName("need_unzip")
            private Boolean need_unzip;
            @SerializedName("model_names")
            private String model_names;
            @SerializedName("requirements")
            private List<?> requirements;

            public EffectListDTO() {
                url_list = new ArrayList<>();
                requirements = new ArrayList<>();
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getResource_id() {
                return resource_id;
            }

            public void setResource_id(String resource_id) {
                this.resource_id = resource_id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

            public List<String> getUrl_list() {
                return url_list;
            }

            public void setUrl_list(List<String> url_list) {
                this.url_list = url_list;
            }

            public Boolean getNeed_unzip() {
                return need_unzip;
            }

            public void setNeed_unzip(Boolean need_unzip) {
                this.need_unzip = need_unzip;
            }

            public String getModel_names() {
                return model_names;
            }

            public void setModel_names(String model_names) {
                this.model_names = model_names;
            }

            public List<?> getRequirements() {
                return requirements;
            }

            public void setRequirements(List<?> requirements) {
                this.requirements = requirements;
            }
        }
    }
}
