package com.dianping.video.utils;

import com.dianping.video.bean.EffectResources;

public class Const {
    // 隐私权限相关
    public static final String UGC_PRIVACY_TOKEN_NOTE = "dp-3ea5c6045f7474fb";

    // 初始化引擎相关
    public static final String host = "http://common.voleai.com";
    public static final String effectLitPath = "/api/effectlist";

    // effectJson 相关
    public static EffectResources effectResources;

    // 存储数据相关
    public static final String PEACOCK_CK_CHANNEL = "dpplatform_peacock_ck";
    public static final String KEY_TEMPLATE_DURATION = "peacock_ck_template_duration";

    // 调试相关
    public static boolean isDebug = false;

}
