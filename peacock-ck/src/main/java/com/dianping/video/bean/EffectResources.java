package com.dianping.video.bean;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 详细说明：素材列表 json 文件对应的 bean 结构
 *
 * @author lvwanyou
 */
@Keep
public class EffectResources {
    public EffectResources() {
        effect_resource_list = new ArrayList<>();
    }

    public List<EffectResourceListDTO> getEffect_resource_list() {
        return effect_resource_list;
    }

    public void setEffect_resource_list(List<EffectResourceListDTO> effect_resource_list) {
        this.effect_resource_list = effect_resource_list;
    }

    @SerializedName("effect_resource_list")
    private List<EffectResourceListDTO> effect_resource_list = new ArrayList<>();

    @Keep
    public static class EffectResourceListDTO {

        @SerializedName("panel")
        private String panel;

        @SerializedName("effect_infos")
        private List<EffectInfosDTO> effect_infos = new ArrayList<>();;

        public String getPanel() {
            return panel;
        }

        public void setPanel(String panel) {
            this.panel = panel;
        }

        public List<EffectInfosDTO> getEffect_infos() {
            return effect_infos;
        }

        public void setEffect_infos(List<EffectInfosDTO> effect_infos) {
            this.effect_infos = effect_infos;
        }

        @Keep
        public static class EffectInfosDTO {

            @SerializedName("requirements")
            private List<?> requirements;

            @SerializedName("resource_id")
            private String resource_id;

            @SerializedName("name")
            private String name;

            @SerializedName("model_names")
            private String model_names;

            @SerializedName("url_list")
            private List<String> url_list;

            @SerializedName("need_unzip")
            private Boolean need_unzip;

            @SerializedName("uri")
            private String uri;

            @SerializedName("id")
            private String id;

            @SerializedName("extras")
            private String extras;

            public List<?> getRequirements() {
                return requirements;
            }

            public void setRequirements(List<?> requirements) {
                this.requirements = requirements;
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

            public String getModel_names() {
                return model_names;
            }

            public void setModel_names(String model_names) {
                this.model_names = model_names;
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

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getExtras() {
                return extras;
            }

            public void setExtras(String extras) {
                this.extras = extras;
            }
        }
    }
}
