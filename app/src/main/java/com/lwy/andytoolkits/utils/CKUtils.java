//package com.example.openglexample.utils;
//
//import static com.dianping.video.utils.Const.KEY_TEMPLATE_DURATION;
//import static com.dianping.video.utils.Const.PEACOCK_CK_CHANNEL;
//
//import android.app.Activity;
//import android.content.ContentResolver;
//import android.content.ContentUris;
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Environment;
//import android.provider.DocumentsContract;
//import android.provider.MediaStore;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import android.view.SurfaceView;
//import android.webkit.MimeTypeMap;
//import android.widget.Toast;
//
//import com.cutsame.solution.player.CutSamePlayer;
//import com.dianping.video.PeacockCKSolution;
//import com.dianping.video.Task.CKTask;
//import com.dianping.video.model.CutSameModel;
//import com.dianping.video.template.model.material.core.VideoMaterial;
//import com.dianping.video.utils.CKHelper;
//import com.dianping.video.utils.DataHelper;
//import com.dianping.video.widget.CKVideoFrameListView;
//import com.meituan.android.cipstorage.CIPStorageCenter;
//import com.ss.android.ugc.cut_ui.MediaItem;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CKUtils {
//    private static final String TAG = "CK-CKUtils";
//
//    // Mock 数据
////    public static String templateUrl1 = "https://s3plus-shon.meituan.net/idealab/baa25f12d05c6f9f49e476119ab10270/xiatianganlvjingvlog_7002464917392293902/template.zip";
////    public static String effectJsonUrl1 = "https://s3plus-shon.meituan.net/idealab/baa25f12d05c6f9f49e476119ab10270/effectResource.json";
////    public static String md51 = "baa25f12d05c6f9f49e476119ab10270";
////    public static int templateType1 = 0;
//    public static String templateUrl1 = "https://s3plus-shon.meituan.net/idealab/55ea967ef4e62ebbc130447af56dc755/xiatianganlvjingvlog_7002464917392293902/template.zip";
//    public static String effectJsonUrl1 = "https://s3plus-shon.meituan.net/idealab/55ea967ef4e62ebbc130447af56dc755/effectResource.json";
//    public static String md51 = "55ea967ef4e62ebbc130447af56dc755";
//    public static int templateType1 = 0;
//
//    //    public static String templateUrl2 = "https://mss-shon.sankuai.com/idealab/cc_1684294048688_917836/template.zip";
////    public static String effectJsonUrl2 = "https://s3plus-shon.meituan.net/idealab/cc_1684294048688_917836/effect_resources_05171138.json";
////    public static String md52 = "bfbcf282edd0c811778b853615206293";
////    public static int templateType2 = 1;
//    public static String templateUrl2 = "https://s3plus-shon.meituan.net/idealab/ad20c2870d03c6cb644389eed007b7d9/cc_1689254172397_107735/template.zip";
//    public static String effectJsonUrl2 = "https://s3plus-shon.meituan.net/idealab/ad20c2870d03c6cb644389eed007b7d9/effectResource.json";
//    public static String md52 = "ad20c2870d03c6cb644389eed007b7d9";
//    public static int templateType2 = 1;
//
//    public static String templateUrl3 = "https://s3plus-shon.meituan.net/idealab/0497061cf113b83485f5c8afe200fe64/jiandandexingfushanshankadian_7002507676275490847/template.zip";
//    public static String effectJsonUrl3 = "https://s3plus-shon.meituan.net/idealab/0497061cf113b83485f5c8afe200fe64/effectResource.json";
//    public static String md53 = "0497061cf113b83485f5c8afe200fe64";
//    public static int templateType3 = 0;
//
//
//    public static String templateUrl = templateUrl2;
//    public static String effectJsonUrl = effectJsonUrl2;
//    public static String md5 = md52;
//    public static int templateType = templateType2;
//    public static ArrayList<VideoMaterial> videoMaterials = new ArrayList<>();
//
//    public static int videoMaterialNum = 7;
//
//    public static void mockCutSameVideoPaths(Context context, int videoMaterialNum, ArrayList<String> paths, ArrayList<Boolean> isPhotoList) {
//        paths.clear();
//        paths.addAll(mockMaterialsResource(context, videoMaterialNum));
//
//        if (paths.size() != videoMaterialNum) {
//            Log.e(TAG, "mockVideoPaths size error");
//        }
//        for (int i = 0; i < videoMaterialNum; i++) {
//            boolean isPhoto = isImageUri(context, Uri.parse(paths.get(i)));
//            isPhotoList.add(isPhoto);
//        }
//    }
//
//    public static ArrayList<VideoMaterial> MockBuildVideoMaterialList(Context context, int videoMaterialNum) {
//        ArrayList<String> paths = new ArrayList<>();
//        ArrayList<Boolean> isPhotoList = new ArrayList<>();
//        ArrayList<VideoMaterial> videoMaterials = new ArrayList<>();
//        mockCutSameVideoPaths(context, videoMaterialNum, paths, isPhotoList);
//        for (int i = 0; i < paths.size(); i++) {
//            VideoMaterial videoMaterial = new VideoMaterial("1");
//            Long oriDuration = isPhotoList.get(i) ? 0 : DataHelper.getVideoDuration(context, paths.get(i));
//
//            videoMaterial.setPath(paths.get(i), isPhotoList.get(i));
//            videoMaterial.setSourceTimeRange(0, oriDuration.intValue());
//            videoMaterials.add(videoMaterial);
//        }
//        return videoMaterials;
//    }
//
//    public static ArrayList<VideoMaterial> MockBuildVideoMaterialList(Context context, int videoMaterialNum, ArrayList<MediaItem> mutableMediaItemList) {
//        ArrayList<String> paths = new ArrayList<>();
//        ArrayList<Boolean> isPhotoList = new ArrayList<>();
//        ArrayList<VideoMaterial> videoMaterials = new ArrayList<>();
//        mockCutSameVideoPaths(context, videoMaterialNum, paths, isPhotoList);
//        for (int i = 0; i < paths.size(); i++) {
//            MediaItem oriMediaItem = mutableMediaItemList.get(i);
//            VideoMaterial videoMaterial = new VideoMaterial("1");
//            String type;
//            Long oriDuration = isPhotoList.get(i) ? oriMediaItem.getOriDuration() : DataHelper.getVideoDuration(context, paths.get(i));
//            ;
//
//            videoMaterial.setPath(paths.get(i), isPhotoList.get(i));
//            videoMaterial.setSourceTimeRange((int) oriMediaItem.getSourceStartTime(), oriDuration.intValue());
//            videoMaterials.add(videoMaterial);
//        }
//        return videoMaterials;
//    }
//
//    public static ArrayList<MediaItem> MockBuildMediaItemList(Context context, ArrayList<MediaItem> mutableMediaItemList) {
//        ArrayList<String> paths = new ArrayList<>();
//        ArrayList<Boolean> isPhotoList = new ArrayList<>();
//
//        mockCutSameVideoPaths(context, 7, paths, isPhotoList);
//        //demo 7个素材
//        for (int i = 0; i < paths.size(); i++) {
//            MediaItem oriMediaItem = mutableMediaItemList.get(i);
//            String type;
//            Long oriDuration = oriMediaItem.getOriDuration();
//
//            if (isPhotoList.get(i)) {
//                type = MediaItem.TYPE_PHOTO;
//            } else {
//                type = MediaItem.TYPE_VIDEO;
//                oriDuration = DataHelper.getVideoDuration(context, paths.get(i));
//            }
//
//            MediaItem clonedItem = new MediaItem(oriMediaItem.getMaterialId(),
//                    oriMediaItem.getTargetStartTime(),
//                    true,
//                    oriMediaItem.getAlignMode(),
//                    oriMediaItem.isSubVideo(),
//                    oriMediaItem.isReverse(),
//                    oriMediaItem.getCartoonType(),
//                    oriMediaItem.getGamePlayAlgorithm(),
//                    oriMediaItem.getWidth(),
//                    oriMediaItem.getHeight(),
//                    oriMediaItem.getClipWidth(),
//                    oriMediaItem.getClipHeight(),
//                    oriMediaItem.getDuration(),
//                    oriDuration,
//                    oriMediaItem.getSource(),
//                    oriMediaItem.getSourceStartTime(),
//                    oriMediaItem.getCropScale(),
//                    oriMediaItem.getCrop(),
//                    type,
//                    oriMediaItem.getMediaSrcPath(),
//                    oriMediaItem.getTargetEndTime(),
//                    oriMediaItem.getVolume(),
//                    oriMediaItem.getRelation_video_group()
//            );
//
//            clonedItem.setSource(paths.get(i));
//            clonedItem.setMediaSrcPath(paths.get(i));
//            mutableMediaItemList.set(i, clonedItem);
//        }
//
//        return mutableMediaItemList;
//    }
//
//    public static CutSameModel mockCreateCutSameModel() {
//        return new CutSameModel(CKUtils.templateUrl, CKUtils.md5, CKUtils.templateType, CKUtils.effectJsonUrl, null);
//    }
//
//    public static CutSameModel mockCreateCutSameModel(String templateUrl, String md5, int templateType, String effectJsonUrl) {
//        CKUtils.templateUrl = templateUrl;
//        CKUtils.md5 = md5;
//        CKUtils.templateType = templateType;
//        CKUtils.effectJsonUrl = effectJsonUrl;
//        return new CutSameModel(CKUtils.templateUrl, CKUtils.md5, CKUtils.templateType, CKUtils.effectJsonUrl, null);
//    }
//
//    public static CutSameModel mockCreateCutSameModel(int videoMaterialNum, Context context) {
//        return new CutSameModel(CKUtils.templateUrl, CKUtils.md5, CKUtils.templateType, CKUtils.effectJsonUrl, MockBuildVideoMaterialList(
//                context,
//                videoMaterialNum));
//    }
//
//    public static CutSameModel mockCreateCutSameModel(String templateUrl, String md5, int templateType, String effectJsonUrl, int videoMaterialNum, Context context) {
//        CKUtils.templateUrl = templateUrl;
//        CKUtils.md5 = md5;
//        CKUtils.templateType = templateType;
//        CKUtils.effectJsonUrl = effectJsonUrl;
//
//        ArrayList<VideoMaterial> videoMaterials = MockBuildVideoMaterialList(
//                context,
//                videoMaterialNum);
//
//        return new CutSameModel(CKUtils.templateUrl, CKUtils.md5, CKUtils.templateType, CKUtils.effectJsonUrl, videoMaterials);
//    }
//
//    public static CutSameModel mockCreateCutSameModelWithoutMaterials(String templateUrl, String md5, int templateType, String effectJsonUrl, int videoMaterialNum) {
//        CKUtils.templateUrl = templateUrl;
//        CKUtils.md5 = md5;
//        CKUtils.templateType = templateType;
//        CKUtils.effectJsonUrl = effectJsonUrl;
//        CKUtils.videoMaterialNum = videoMaterialNum;
//        return new CutSameModel(CKUtils.templateUrl, CKUtils.md5, CKUtils.templateType, CKUtils.effectJsonUrl);
//    }
//
//    private static List<String> mockMaterialsResource(Context context, int videoMaterialNum) {
//        ArrayList<String> paths = new ArrayList<>();
//        String[] projection = new String[]{
//                MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.DISPLAY_NAME,
//                MediaStore.Images.Media.DATE_TAKEN
//        };
//
//        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
//
//        try (Cursor cursor = context.getContentResolver().query(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                projection,
//                null,
//                null,
//                sortOrder
//        )) {
//            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
//
//            int count = 0;
//            while (cursor.moveToNext() && count < videoMaterialNum) {
//                long id = cursor.getLong(idColumn);
//                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//                paths.add(contentUri.toString());
//                count++;
//            }
//        }
//
//        return paths;
//    }
//
//    public static boolean isImageUri(Context context, Uri uri) {
//        if (uri == null || context == null) {
//            return false;
//        }
//
//        // 获取文件MIME类型
//        ContentResolver contentResolver = context.getContentResolver();
//        String mimeType = contentResolver.getType(uri);
//
//        // 也可以通过文件扩展名判断
//        if (mimeType == null) {
//            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
//            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
//        }
//
//        // 检查MIME类型是否以"image/"开头
//        return mimeType != null && mimeType.startsWith("image/");
//    }
//
//    public static void getDurationFromPlayer(final Context context, final CutSameModel cutSameModel, final GetDurationFromPlayerListener getDurationFromPlayerListener) {
//        long duration = CIPStorageCenter.instance(context, PEACOCK_CK_CHANNEL).getLong(KEY_TEMPLATE_DURATION + "_" + CKUtils.md5, -1);
//        if (duration == -1) {
//            final CKTask ckTask = PeacockCKSolution.getInstance().getTaskOrNew(cutSameModel);
//            if (ckTask != null) {
//                PeacockCKSolution.getInstance().prepareAndCompressTemplateSource(ckTask.getCutSameModel(), new PeacockCKSolution.CKProgressDealSourceListener() {
//                    @Override
//                    public void onProgress(float progress) {
//
//                    }
//
//                    @Override
//                    public void onSuccess() {
//                        CutSamePlayer cutSamePlayer = ckTask.createCutSamePlayer(new SurfaceView(context), ckTask.getCutSameSource());
//                        if (cutSamePlayer != null) {
//                            ckTask.setCutSamePlayer(ckTask.getCutSameModelId() + CKTask.GetFrameMethodName, cutSamePlayer);
//                            ckTask.preparePlay(cutSamePlayer, ckTask.getMutableMediaItemList(), ckTask.getMutableTextItemList(), context);
//                            getDurationFromPlayerListener.onSuccess(cutSamePlayer.getDuration());
//                        } else {
//                            getDurationFromPlayerListener.onError("create player error.");
//                        }
//                    }
//
//                    @Override
//                    public void onError(int errorCode, @Nullable String errorMsg) {
//                        getDurationFromPlayerListener.onError(errorMsg);
//                    }
//                });
//            } else {
//                getDurationFromPlayerListener.onError("get CKTask fail when setupAdapter, need call PeacockCKSolution.getInstance().prepareTemplateSource()!");
//
//                CKHelper.novaCodeLogE(CKVideoFrameListView.class, TAG, "get CKTask fail when setupAdapter, need call PeacockCKSolution.getInstance().prepareTemplateSource()!");
//            }
//        } else {
//            getDurationFromPlayerListener.onSuccess(duration);
//        }
//    }
//
//    private static boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri.getAuthority());
//    }
//
//    private static boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }
//
//    private static boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }
//
//    public static String getPathFromUri(Context context, Uri uri) {
//        String path = null;
//        try {
//            if (DocumentsContract.isDocumentUri(context, uri)) {
//                // 如果是Document类型的URI，则通过Document ID来进行解析
//                String documentId = DocumentsContract.getDocumentId(uri);
//                if (isExternalStorageDocument(uri)) {
//                    // 如果是外部存储器的URI，则获取SD卡的路径
//                    String[] split = documentId.split(":");
//                    if (split.length >= 2) {
//                        String type = split[0];
//                        if ("primary".equalsIgnoreCase(type)) {
//                            path = Environment.getExternalStorageDirectory() + "/" + split[1];
//                        }
//                    }
//                } else if (isDownloadsDocument(uri)) {
//                    // 如果是Downloads文件夹的URI，则获取Downloads文件夹的路径
//                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
//                    path = getDataColumn(context, contentUri, null, null);
//                } else if (isMediaDocument(uri)) {
//                    // 如果是Media类型的URI，则获取Media文件的路径
//                    String[] split = documentId.split(":");
//                    if (split.length >= 2) {
//                        String type = split[0];
//                        Uri contentUri = null;
//                        if ("image".equals(type)) {
//                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                        } else if ("video".equals(type)) {
//                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                        } else if ("audio".equals(type)) {
//                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                        }
//                        String selection = "_id=?";
//                        String[] selectionArgs = new String[]{split[1]};
//                        path = getDataColumn(context, contentUri, selection, selectionArgs);
//                    }
//                }
//            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
//                // 如果是Content类型的URI，则直接通过ContentResolver进行解析
//                path = getDataColumn(context, uri, null, null);
//                if (path == null && uri.getPath().endsWith(".zip")) {
//                    path = uri.getPath();
//                }
//            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
//                // 如果是File类型的URI，则直接获取文件路径
//                path = uri.getPath();
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "getPathFromUri error, and " + e.getMessage());
//        }
//        return path;
//    }
//
//    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
//        String path = null;
//        String[] projection = {MediaStore.Images.Media.DATA};
//        Cursor cursor = null;
//        try {
//            Log.d(TAG, "getDataColumn uri: " + uri.toString());
//            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                path = cursor.getString(columnIndex);
//            }
//        } catch (IllegalArgumentException exception) {
//            Log.e(TAG, "_data is not exist, and " + exception.getMessage());
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return path;
//    }
//
//    public static void checkBuildGradleSetting(Activity activity){
//        try {
//            PackageManager manager = activity.getPackageManager();
//            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
//            String applicationId = activity.getPackageName(); // 这就是applicationId
//            int versionCode = info.versionCode;
//            String versionName = info.versionName;
//
//            if (!("com.dianping.v1".equals(applicationId) && versionCode >= 110300)) {
//                Toast.makeText(activity, "无法使用火山视频模板功能, 请前往 app module 的 build.gradle 按照注释提示更改 applicationId 信息", Toast.LENGTH_LONG).show();
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public interface GetDurationFromPlayerListener {
//        void onSuccess(long duration);
//
//        void onError(String errorMsg);
//    }
//}
