package com.example.openglexample.model;

/**
 * 音频处理配置参数
 *
 * @api
 */
public class AudioConfigInfo {

    /**
     * 音频采样率
     */
    public int sampleRate = TemplateConstant.AUDIO_SAMPLE_RATE;

    /**
     * 音频声道数
     */
    public int channelCount = TemplateConstant.AUDIO_CHANNEL_COUNT;

    /**
     * 音频处理窗口大小
     */
    public int maxInputSize = TemplateConstant.AUDIO_BUFFER_SIZE * 2;

    /**
     * 音频采样位数
     */
    public int bitWidth = TemplateConstant.AUDIO_BIT_WIDTH;

    /**
     * 音频比特率
     */
    public int bitRate = TemplateConstant.AUDIO_BIT_RATE;

    /**
     * 是否启用软编码
     */
    public boolean enableSoftEncode = false;

    /**
     * 是否启用软解码
     */
    public boolean enableSoftDecode = false;

    /**
     * 是否允许不处理音频
     */
    public boolean enableNoProcessAudio = false;

    /**
     * 开启音频异步处理器
     */
    public boolean enableAsyncEncode = false;


    @Override
    public String toString() {
        return "AudioConfigInfo{" +
                "sampleRate=" + sampleRate +
                ", channelCount=" + channelCount +
                ", maxInputSize=" + maxInputSize +
                ", bitWidth=" + bitWidth +
                ", bitRate=" + bitRate +
                ", enableSoftEncode=" + enableSoftEncode +
                ", enableSoftDecode=" + enableSoftDecode +
                ", enableNoProcessAudio=" + enableNoProcessAudio +
                ", enableAsyncEncode=" + enableAsyncEncode +
                '}';
    }
}


/**
 * 模板常量
 */
class TemplateConstant {
    /**
     * 模板音频bit位数
     */
    public static final int AUDIO_BIT_WIDTH = 16;
    /**
     * 模板音频采样率
     */
    public static final int AUDIO_SAMPLE_RATE = 44100;
    /**
     * 模板音频声道数
     */
    public static final int AUDIO_CHANNEL_COUNT = 2;
    /**
     * 模板音频数据一次buffer大小
     */
    public static final int AUDIO_BUFFER_SIZE = 2048; //todo 待确认步长，步长4096比2048能节省一定时间
    /**
     * 模板音频比特数
     */
    public static final int AUDIO_BIT_RATE = 128000;

}