package com.dianping.video.model;

import com.dianping.video.template.model.material.core.VideoMaterial;

import java.util.ArrayList;

public class CutSameModel {
    /**
     * 模版 url
     */
    private String templateUrl;
    /**
     * 模版 md5
     */
    private String md5;
    /**
     * 模版类型：1 指的是通过 templator 做的模版，0 指的是另一批早期的模版
     */
    private int templateType;
    /**
     * effectJson url
     */
    private String effectJsonUrl;
    /**
     * 物料列表
     */
    private ArrayList<VideoMaterial> videoMaterials = new ArrayList<>();

    public CutSameModel() {
        this("");
    }

    public CutSameModel(String templateUrl) {
        this(templateUrl, "");
    }

    public CutSameModel(String templateUrl, String md5) {
        this(templateUrl, md5, 1);
    }

    public CutSameModel(String templateUrl, String md5, int templateType) {
        this(templateUrl, md5, templateType, "");
    }

    public CutSameModel(String templateUrl, String md5, int templateType, String effectJsonUrl) {
        this(templateUrl, md5, templateType, effectJsonUrl, null);
    }

    public CutSameModel(String templateUrl, String md5, int templateType, String effectJsonUrl, ArrayList<VideoMaterial> videoMaterials) {
        this.templateUrl = templateUrl;
        this.md5 = md5;
        this.templateType = templateType;
        this.effectJsonUrl = effectJsonUrl;
        this.videoMaterials = videoMaterials;
    }

    public String getTemplateUrl() {
        return templateUrl;
    }

    public void setTemplateUrl(String templateUrl) {
        this.templateUrl = templateUrl;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getTemplateType() {
        return templateType;
    }

    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }

    public String getEffectJsonUrl() {
        return effectJsonUrl;
    }

    public void setEffectJsonUrl(String effectJsonUrl) {
        this.effectJsonUrl = effectJsonUrl;
    }

    public ArrayList<VideoMaterial> getVideoMaterials() {
        return videoMaterials;
    }

    public void setVideoMaterials(ArrayList<VideoMaterial> videoMaterials) {
        this.videoMaterials = videoMaterials;
    }
}
