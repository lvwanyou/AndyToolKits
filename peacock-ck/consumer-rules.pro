
-keep class com.bytedance.labcv.** { *; }
-keep class com.bef.effectsdk.** { *; }
#keep住vesdk的某些类 防止vedemo加载so时的启动crash
-keep class com.ss.** { *; }


-keep class com.ss.mediakit.** {*;}
-keep class com.ss.ttm.** {*;}
-keep class com.ss.ttvideoengine.** {*;}
-keep class com.ss.mediakit.medialoader.** {*;}
-keep class com.ss.texturerender.** {*;}

-keep class com.ss.ttm.** {*;}
# keep ttpreloader
-keep class com.ss.ttpreloader.** {*;}

# effectmanager
-keep class com.ss.android.ugc.effectmanager.effect.model.** {* ;}
-keep class com.ss.android.ugc.effectmanager.common.model.** {* ;}
-keep class com.ss.android.ugc.effectmanager.link.model.** {* ;}
-keep class com.ss.android.ugc.effectmanager.model.**{* ;}

# cut same sdk
-keep class com.bytedance.ies.cutsame.veadapter.** {*;}
-keep class com.bytedance.ies.cutsameconsumer.templatemodel.** {*;}
-keep interface com.bytedance.ies.cutsame.resourcefetcher.ResourceFetcher {*;}
-keep interface com.bytedance.ies.cutsame.resourcefetcher.ResourceFetcherCallBack {*;}
-keep class com.bytedance.ies.cutsame.resourcefetcher.JniResourceFetcherCallback {*;}
-keep class com.bytedance.ies.cutsame.resourcefetcher.EffectResourceFetcher$EffectItem {*;}
-keep class com.bytedance.ies.cutsame.resourcefetcher.NetworkFileFetcher$DownloadItem {*;}
-keep interface com.ss.android.ttve.nativePort.TEVideoUtils$ExecuteCommandListener { *; }
-keep class com.bytedance.ies.cutsame.cut_android.PrepareListener {*;}
-keep class com.bytedance.ies.cutsame.cut_android.TemplateInfoListener {*;}
-keep class  com.bytedance.ies.cutsame.cut_android.TemplatePlayer {*;}
-keep class com.ss.android.ugc.model.VECompileParam {*;}

-keep class com.bytedance.ies.cutsame.util.MediaUtil {
    *** getVideoSize(...);
}
-keep class com.bytedance.ies.cutsame.util.Size {*;}

-keep class com.ss.android.ugc.cut_ui.TextItem{*;}
-keep class com.ss.android.ugc.cut_ui.MediaItem{*;}
-keep class com.ss.android.ugc.cut_ui.ItemCrop{*;}
-keep class com.ss.android.ugc.cut_ui.MediaItem$Companion{*;}
-keep class com.bytedance.ies.cutsame.util.** {*;}
-keep class com.ss.android.ugc.cut_log.LogUtil{*;}
-keep class com.ss.android.ugc.cut_log.LogWrapper{*;}
-keep interface com.ss.android.ugc.cut_log.LogIF{*;}
-keep class com.ss.android.ugc.cut_log.LogConfig{*;}
-keep class com.ss.android.ugc.cut_log.LogConfig$Builder{*;}
-keep enum com.ss.android.ugc.cut_log.LogIF$LOG_LEVER {*;}
-keep class com.ss.android.ugc.cut_downloader.** {*;}
-keep class com.ss.android.ugc.cut_downloader_simple.** {*;}

#VE SDK
-keep class com.bef.effectsdk.** {*;}
-keep class com.ss.android.vesdk.VESDK {*;}
-keep class com.ss.android.vesdk.VEConfigItem {*;}
-keep class com.ss.android.vesdk.VEUtils {*;}
-keep class com.ss.android.vesdk.VEEditor {*;}
-keep class com.ss.android.vesdk.VEVideoEncodeSettings {*;}
-keep public class com.ss.android.vesdk.utils.TEPlanUtils {*;}

-keep enum com.ss.android.vesdk.VEEditor$VIDEO_RATIO  {*;}
-keep enum com.ss.android.vesdk.ROTATE_DEGREE {*;}

-keep interface com.ss.android.vesdk.VEListener {*;}
-keep interface com.ss.android.vesdk.VEListener$VEEditorCompileListener{*;}

-keep class com.ss.android.ttve.nativePort.TEVideoUtils { *; }
-keep interface com.ss.android.ttve.nativePort.TEVideoUtils$ExecuteCommandListener { *; }

-keep class com.bytedance.ies.cutsame.util.VEUtils {
    <init>(...);
    *** calculateAveCurveSpeed(...);
    *** transGif2Png(...);
 }


# cv
-keep class com.bef.effectsdk.** { *; }
-keep class com.bytedance.labcv.effectsdk.** { *; }
-keep class com.ss.android.ugc.effectmanager.model.**{*;}

# sdl && ffmpeg
-keep class org.libsdl.app.** { *; }
-keep class com.ss.android.medialib.FFMpegInvoker{*;}
-keep class com.ss.android.medialib.StickerInvoker{*;}
-keep class com.ss.android.medialib.RecordInvoker{*;}
-keep class com.ss.android.medialib.MarkInvoker{*;}
-keep class com.ss.android.medialib.style.StyleActionListener{*;}

## Android architecture components: Lifecycle
# LifecycleObserver's empty constructor is considered to be unused by proguard
-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}
# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends android.arch.lifecycle.ViewModel {
    <init>(...);
}
# keep Lifecycle State and Event enums values
-keepclassmembers class android.arch.lifecycle.Lifecycle$State { *; }
-keepclassmembers class android.arch.lifecycle.Lifecycle$Event { *; }
# keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @android.arch.lifecycle.OnLifecycleEvent *;
}

-keep class com.bytedance.ies.cutsameconsumer.** {
    public <methods>;
    static <fields>;
}
-keep class com.bytedance.ies.nle.editor_jni.** {
    public <methods>;
    static <fields>;
}

-keep class com.bytedance.ies.nle.editor_jni.NLEEditorJniJNI {
    *** SwigDirector_*(...);
}

-keep class com.bytedance.ies.nle.editor_jni.NLEModel {
    private transient long swigCPtr;
}
-keep enum com.bytedance.ies.nle.editor_jni.NLEError { *; }
-keep enum com.bytedance.ies.nle.editor_jni.NLEDoneType { *; }
-keep enum com.bytedance.ies.nle.editor_jni.LogLevel { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLEEditorListener { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLEBranchListener { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLEResourceSynchronizer { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLEResourceFetchCallback { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLEChangeListener { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLELoggerListener { *; }
-keep class com.bytedance.ies.nle.editor_jni.INLEMonitor { *; }
-keep class com.bytedance.ies.nle.editor_jni.SWIGTYPE_p_va_list { *; }

-keep class com.bytedance.ies.nleedtor.*{
*;
}

-keep class com.bytedance.ies.nle.editor_jni.*{
          *;
}

-keep class com.bytedance.ies.nlemediajava.keyframe.bean.*{
  *;
}

-keepclasseswithmembernames class com.bytedance.ies.nleedtor.* {
      native <methods>;
      static <methods>;
  }
  
  -keep class com.vesdk.verecorder.record.demo.view.CountDownDialog{
    *;
  }
  -keep class com.vesdk.verecorder.record.demo.view.CountDownDialog$Callback{
    *;
  }
  -keep class com.vesdk.verecorder.record.demo.view.RecordTabView$OnSelectedListener{
    *;
  }
  -keep class com.vesdk.verecorder.record.demo.view.RecordTabView{
    *;
  }
  -keep class com.vesdk.verecorder.record.demo.view.RecordTabView$ItemHolder{
    *;
  }

