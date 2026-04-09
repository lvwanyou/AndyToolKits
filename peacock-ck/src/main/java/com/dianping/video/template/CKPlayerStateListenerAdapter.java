package com.dianping.video.template;

interface CKPlayerStateListener {
    /**
     * 播放器prepare完成，即将触发 start()
     */
    void onPlayerPrepareOk();

    void onPlayerStart();

    /**
     * 播放器已经开始播放了，第一帧渲染完成
     */
    void onPlayerFirstFrameOk();

    /**
     * 播放器是否播放中
     */
    void onPlayerPlaying(boolean isPlaying);

    /**
     * 播放器销毁
     */
    void onPlayerDestroy();

    /**
     * 播放器进度回调
     *
     * @param progressTime     播放器进度时间点
     * @param progressDuration 播放器进度总时长
     */
    void onPlayerProgress(long progressTime, long progressDuration);

    /**
     * 播放器是否播放完成
     */
    void onPlayEof();
}

public abstract class CKPlayerStateListenerAdapter implements CKPlayerStateListener {
    @Override
    public void onPlayerPrepareOk() {
    }

    @Override
    public void onPlayerStart() {
    }

    @Override
    public void onPlayerFirstFrameOk() {
    }

    @Override
    public void onPlayerPlaying(boolean isPlaying) {
    }

    @Override
    public void onPlayerDestroy() {
    }

    @Override
    public void onPlayerProgress(long progressTime, long progressDuration) {
    }

    @Override
    public void onPlayEof() {
    }
}



