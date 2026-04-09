package com.dianping.video.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.webkit.URLUtil;

import com.dianping.video.template.model.material.core.VideoMaterial;
import com.ss.android.ugc.cut_ui.MediaItem;

import java.util.ArrayList;
import java.util.Collections;


public class DataHelper {
    /**
     * model 转换： {@link VideoMaterial} ->  {@link MediaItem}
     *
     * @param videoMaterials
     * @param mediaItems
     * @return
     */
    public static ArrayList<MediaItem> convertVideoMaterials2MediaItems(ArrayList<VideoMaterial> videoMaterials, ArrayList<MediaItem> mediaItems) {
        if (videoMaterials.size() < mediaItems.size()) return null;

        for (int i = 0; i < mediaItems.size(); i++) {
            MediaItem oriMediaItem = mediaItems.get(i);
            MediaItem clonedItem = new MediaItem(oriMediaItem.getMaterialId(),
                    oriMediaItem.getTargetStartTime(),
                    true,
                    oriMediaItem.getAlignMode(),
                    oriMediaItem.isSubVideo(),
                    oriMediaItem.isReverse(),
                    oriMediaItem.getCartoonType(),
                    oriMediaItem.getGamePlayAlgorithm(),
                    oriMediaItem.getWidth(),
                    oriMediaItem.getHeight(),
                    oriMediaItem.getClipWidth(),
                    oriMediaItem.getClipHeight(),
                    oriMediaItem.getDuration(),
                    videoMaterials.get(i).getSourceTimeDuration(),  // 物料时长
                    videoMaterials.get(i).getPath(),    // 物料路径
                    videoMaterials.get(i).getSourceTimeStart(),  // 物料起始播放时长
                    oriMediaItem.getCropScale(),
                    oriMediaItem.getCrop(),
                    videoMaterials.get(i).isPhoto() ? MediaItem.TYPE_PHOTO : MediaItem.TYPE_VIDEO,  // 物料类型
                    videoMaterials.get(i).getPath(),    // 物料路径
                    oriMediaItem.getTargetEndTime(),
                    oriMediaItem.getVolume(),
                    oriMediaItem.getRelation_video_group()
            );
            mediaItems.set(i, clonedItem);
        }
        return mediaItems;
    }

    /**
     * 获取 video 时长
     * @param context
     * @param videoPath
     * @return
     */
    public static long getVideoDuration(Context context, String videoPath) {
        long duration = 0;
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            if (URLUtil.isContentUrl(videoPath)) {
                retriever.setDataSource(context, Uri.parse(videoPath));
            } else {
                retriever.setDataSource(videoPath);
            }
            duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (retriever != null) {
                retriever.release();
            }
        }
        return duration;
    }

}
