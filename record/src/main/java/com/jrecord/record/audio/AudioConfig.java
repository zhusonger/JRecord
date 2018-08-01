package com.jrecord.record.audio;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 音频录制配置
 * 定义采样率(横轴)，声道数，音频格式(PCM8/PCM16)，比特率(纵轴)等
 */
public class AudioConfig implements Parcelable {
    public static final ThreadGroup AUDIO_GROUP = new ThreadGroup("audio");
    private int audioSource = MediaRecorder.AudioSource.DEFAULT;
    /*
     8khz：电话等使用，对于记录人声已经足够使用。

     22.05khz：广播使用频率。

     44.1kb：音频CD。

     48khz：DVD、数字电视中使用。

     96khz-192khz：DVD-Audio、蓝光高清等使用。
     */
    private int sampleRateInHz = 44100; // 采样率
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO; // 声道
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 音频格式
    private int bitRate = 64000; // 比特率 64kb/s aac:64kb/s mp3:128kb/s
    private String mime = "audio/mp4a-latm";

    public AudioConfig() {
    }

    protected AudioConfig(Parcel in) {
        audioSource = in.readInt();
        sampleRateInHz = in.readInt();
        channelConfig = in.readInt();
        audioFormat = in.readInt();
        bitRate = in.readInt();
        mime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(audioSource);
        dest.writeInt(sampleRateInHz);
        dest.writeInt(channelConfig);
        dest.writeInt(audioFormat);
        dest.writeInt(bitRate);
        dest.writeString(mime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AudioConfig> CREATOR = new Creator<AudioConfig>() {
        @Override
        public AudioConfig createFromParcel(Parcel in) {
            return new AudioConfig(in);
        }

        @Override
        public AudioConfig[] newArray(int size) {
            return new AudioConfig[size];
        }
    };

    public int audioSource() {
        return audioSource;
    }

    public AudioConfig audioSource(int audioSource) {
        this.audioSource = audioSource;
        return this;
    }

    public int sampleRateInHz() {
        return sampleRateInHz;
    }

    public AudioConfig sampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
        return this;
    }

    public int channelConfig() {
        return channelConfig;
    }

    public AudioConfig channelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
        return this;
    }

    public int audioFormat() {
        return audioFormat;
    }

    public AudioConfig audioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
        return this;
    }

    public int bitRate() {
        return bitRate;
    }

    public AudioConfig bitRate(int bitRate) {
        this.bitRate = bitRate;
        return this;
    }

    public String mime() {
        return mime;
    }

    public AudioConfig mime(String mime) {
        this.mime = mime;
        return this;
    }

    @Override
    public String toString() {
        return "AudioConfig{" +
                "audioSource=" + audioSource +
                ", sampleRateInHz=" + sampleRateInHz +
                ", channelConfig=" + channelConfig +
                ", audioFormat=" + audioFormat +
                ", bitRate=" + bitRate +
                ", mime='" + mime + '\'' +
                '}';
    }
}
