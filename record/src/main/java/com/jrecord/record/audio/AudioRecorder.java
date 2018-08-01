package com.jrecord.record.audio;

import android.media.AudioRecord;

/**
 * 音频录制线程,使用系统自带的AudioRecord的录制线程
 * 其他音频录制另外新建线程来实现录制功能
 */
public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    private AudioRecord mRecord;
    private AudioConfig mConfig;

    private AudioRecorder(){}
    private static AudioRecorder INSTANCE = new AudioRecorder();
    public static AudioRecorder getInstance() {
        return INSTANCE;
    }

    public void init(AudioConfig config) {
        mConfig = config;
    }

    public void start() {
        if (null == mConfig) {
            mConfig = new AudioConfig();
        }
    }


}
