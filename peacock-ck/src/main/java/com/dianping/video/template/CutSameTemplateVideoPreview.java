package com.dianping.video.template;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

import com.cutsame.solution.player.BasePlayer;
import com.cutsame.solution.player.CutSamePlayer;
import com.cutsame.solution.player.GetImageListener;
import com.cutsame.solution.player.PlayerStateListener;
import com.dianping.codelog.NovaCodeLog;
import com.dianping.video.PeacockCKSolution;
import com.dianping.video.Task.CKTask;
import com.dianping.video.model.CutSameModel;
import com.ss.android.ugc.cut_ui.MediaItem;
import com.ss.android.ugc.cut_ui.TextItem;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * 剪同款视频预览组件
 *
 * @author wuxuelei & lvwanyou
 * @api
 */
public class CutSameTemplateVideoPreview extends SurfaceView implements ITemplateVideoPlay {

    private static final String TAG = "CK-TemplateVideoPreview";

    private PlayerStateListener playerStateListener;
    private CKPlayerStateListener ckPlayerStateListener;
    private CutSamePlayer cutSamePlayer;

    private Boolean isMove = false;

    private CutSameModel cutSameModel;
    private CKTask ckTask;
    private int progressUnit = 100;

    public CutSameTemplateVideoPreview(Context context) {
        this(context, null);
    }

    public CutSameTemplateVideoPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CutSameTemplateVideoPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void isMove(Boolean isMove) {
        this.isMove = isMove;
    }

    public void init(CutSameModel cutSameModel) {
        this.cutSameModel = cutSameModel;
        this.ckTask = PeacockCKSolution.getInstance().getTaskOrNew(this.cutSameModel);

        playerStateListener = new PlayerStateListener() {
            @Override
            public void onFirstFrameRendered() {
                if (ckPlayerStateListener != null) {
                    ckPlayerStateListener.onPlayerFirstFrameOk();
                }
            }

            @Override
            public void onChanged(int state) {
                switch (state) {
                    case PlayerStateListener.PLAYER_STATE_PREPARED: {
                        if (cutSamePlayer != null) {
                            ArrayList<TextItem> textItems = cutSamePlayer.getTextItems(true);
                            // 视频合成之后，获取文本
                            if (ckTask != null) {
                                ckTask.setMutableTextItemList(textItems);
                            }
                        }

                        if (ckPlayerStateListener != null) {
                            ckPlayerStateListener.onPlayerPrepareOk();
                            ckPlayerStateListener.onPlayerStart();
                        }

                        playVideo();
                        break;
                    }
                    case PlayerStateListener.PLAYER_STATE_PLAYING:
                        if (ckPlayerStateListener != null) {
                            ckPlayerStateListener.onPlayerPlaying(true);
                        }
                        break;
                    case PlayerStateListener.PLAYER_STATE_DESTROYED:
                        if (ckPlayerStateListener != null) {
                            ckPlayerStateListener.onPlayerDestroy();
                        }
                        break;
                    default:    // 包括：PLAYER_STATE_ERROR、PLAYER_STATE_IDLE、PLAYER_STATE_PAUSED 及 其他
                        if (ckPlayerStateListener != null) {
                            ckPlayerStateListener.onPlayerPlaying(false);
                        }
                }
            }

            @Override
            public void onPlayProgress(long process) {
                if (ckPlayerStateListener != null && cutSamePlayer != null) {
                    ckPlayerStateListener.onPlayerProgress(process, cutSamePlayer.getDuration());
                }
            }

            @Override
            public void onPlayEof() {
                if (ckPlayerStateListener != null) {
                    ckPlayerStateListener.onPlayEof();
                    ckPlayerStateListener.onPlayerPlaying(false);
                }
            }

            @Override
            public void onPlayError(int what, @NonNull String extra) {

            }
        };
    }


    /**
     * 初始化 player
     */
    private void initPlayer() {
        if (ckTask == null) return;

        cutSamePlayer = ckTask.createCutSamePlayer(CutSameTemplateVideoPreview.this, ckTask.getCutSameSource());
        if (cutSamePlayer != null) {
            ckTask.preparePlay(cutSamePlayer, ckTask.getMutableMediaItemList(), ckTask.getMutableTextItemList(), getContext());
            cutSamePlayer.registerPlayerStateListener(playerStateListener);
        }
    }


    public void seeking(int progressRatio) {
        if (cutSamePlayer != null) {
            int currentTime = (int) (progressRatio * cutSamePlayer.getDuration() / progressUnit);
            cutSamePlayer.seeking(currentTime);
            NovaCodeLog.i(CutSameTemplateVideoPreview.class, TAG, "seeking, progressRatio:" + progressRatio + ", currentTime:" + currentTime);
        }
    }


    /**
     * 获取素材
     *
     * @param index
     * @return
     */
    public MediaItem getMediaItem(int index, ArrayList<MediaItem> mutableMediaItemList) {
        if (mutableMediaItemList == null || mutableMediaItemList.size() <= index) {
            return null;
        }
        return mutableMediaItemList.get(index);
    }


    /**
     * 获取文字
     *
     * @return
     */
    public List<TextItem> getTextItems() {
        if (cutSamePlayer != null) {
            return cutSamePlayer.getTextItems(false);
        }
        return null;
    }


    /**
     * 更新文字
     *
     * @param index
     * @param text
     */
    public void updateText(int index, String text) {
        if (cutSamePlayer != null) {
            List<TextItem> textItems = getTextItems();
            if (textItems != null && textItems.size() > index) {
                TextItem originTextItem = textItems.get(index);
                if (originTextItem != null) {
                    cutSamePlayer.updateText(textItems.get(index).getMaterialId(), text);
                }
            }
        }
    }

    /**
     * 更新素材
     *
     * @param index
     * @param processItem new material
     */
    public void updateMedia(int index, MediaItem processItem, ArrayList<MediaItem> mutableMediaItemList) {
        if (cutSamePlayer != null && mutableMediaItemList != null) {
            MediaItem originMediaItem = mutableMediaItemList.get(index);
            MediaItem resultMediaItem = originMediaItem.copy(
                    originMediaItem.getMaterialId(),
                    originMediaItem.getTargetStartTime(),
                    originMediaItem.isMutable(),
                    originMediaItem.getAlignMode(),
                    originMediaItem.isSubVideo(),
                    originMediaItem.isReverse(),
                    originMediaItem.getCartoonType(),
                    originMediaItem.getGamePlayAlgorithm(),
                    originMediaItem.getWidth(),
                    originMediaItem.getHeight(),
                    originMediaItem.getClipWidth(),
                    originMediaItem.getClipHeight(),
                    originMediaItem.getDuration(),
                    originMediaItem.getOriDuration(),
                    processItem.getSource(),
                    processItem.getSourceStartTime(),
                    processItem.getCropScale(),
                    processItem.getCrop(),
                    processItem.getType(),
                    processItem.getMediaSrcPath(),
                    originMediaItem.getTargetEndTime(),
                    originMediaItem.getVolume(),
                    originMediaItem.getRelation_video_group()
            );

            mutableMediaItemList.set(index, resultMediaItem);
            cutSamePlayer.updateMedia(originMediaItem.getMaterialId(), resultMediaItem);
        }
    }


    /**
     * 设置音量
     *
     * @param selectedMediaItem
     * @param volumeValue       范围 0～1
     */
    public void setVolume(MediaItem selectedMediaItem, float volumeValue) {
        cutSamePlayer.setVolume(selectedMediaItem.getMaterialId(), volumeValue);
    }


    /**
     * 获取音量
     */
    public void getVolume(String materialId) {
        cutSamePlayer.getVolume(materialId);
    }


    @Override
    public boolean isPlaying() {
        if (cutSamePlayer != null) {
            return cutSamePlayer.getState() == BasePlayer.PlayState.PLAYING;
        }
        return false;
    }


    @Override
    public void pauseVideo() {
        if (cutSamePlayer != null && cutSamePlayer.getState() == BasePlayer.PlayState.PLAYING) {
            cutSamePlayer.pause();
        }
    }


    @Override
    public void playVideo() {
        if (cutSamePlayer != null) {
            cutSamePlayer.start();
        } else {
            initPlayer();
        }
    }


    @Override
    public void resumeVideo() {
        if (cutSamePlayer != null) {
            cutSamePlayer.start();
        } else {
            initPlayer();
        }
    }


    @Override
    public void stopVideo() {

    }


    @Override
    public void seekTo(long millisecond, int seekMode) {
        if (cutSamePlayer != null) {
            cutSamePlayer.seekTo((int) millisecond, true, null);
        }
    }


    public void seekTo(int progressRatio) {
        if (cutSamePlayer != null) {
            int currentTime = (int) (progressRatio * cutSamePlayer.getDuration() / progressUnit);
            cutSamePlayer.seekTo(currentTime, true, null);
            NovaCodeLog.i(CutSameTemplateVideoPreview.class, TAG, "seekTo, currentTime:" + currentTime);
        }
    }


    public void setCKPlayerStateListener(CKPlayerStateListener cKPlayerStateListener) {
        this.ckPlayerStateListener = cKPlayerStateListener;
    }


    /**
     * 预览控件的抽帧功能；区别于 CKTask 中的抽帧功能，此处复用预览控件的 player，以规避重复执行耗时操作 preparePlay()。
     * 调用时机需在 prepareSource 及 composeSource 之后。
     *
     * @param timeStamps
     * @param thumbnailWidth
     * @param thumbnailHeight
     * @param getImageListener
     */
    public void getVideoFrameWithTime(int[] timeStamps, int thumbnailWidth, int thumbnailHeight, final PeacockCKSolution.CKGetImageListener getImageListener) {
        if (cutSamePlayer == null) {
            initPlayer();
        }
        cutSamePlayer.getVideoFrameWithTime(timeStamps,
                thumbnailWidth,
                thumbnailHeight,
                new GetImageListener() {
                    @Override
                    public void onGetImageData(byte[] bytes, int pts, int width, int height, float score) {
                        if (bytes != null) {
                            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));

                            getImageListener.frameBitmap(String.valueOf(pts), bitmap);
                        } else {
                            // 取帧结束后需要cancel，不然会停留在取帧状态
                            cutSamePlayer.cancelGetVideoFrames();

                            getImageListener.frameBitmap("", null);
                        }
                    }
                });
    }


    /**
     * 释放资源
     */
    public void release() {
        if (cutSamePlayer != null) {
            cutSamePlayer.release();
            cutSamePlayer = null;
        }

        // 注销回调
        if (this.ckPlayerStateListener != null) {
            this.ckPlayerStateListener = null;
        }
    }


    public void registerListener() {
        if (cutSamePlayer != null) {
            cutSamePlayer.registerPlayerStateListener(playerStateListener);
        }
    }


    public void unRegisterListener() {
        if (cutSamePlayer != null) {
            cutSamePlayer.unRegisterPlayerStateListener(playerStateListener);
        }
    }


    public void unregisterCKPlayerStateListener() {
        this.ckPlayerStateListener = null;
    }
}