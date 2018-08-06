package com.jrecord;

import android.media.MediaFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.jrecord.common.ILog;
import com.jrecord.common.base.BaseActivity;
import com.jrecord.record.audio.AudioConfig;
import com.jrecord.record.audio.AudioEncoder;
import com.jrecord.record.audio.AudioRecorder;
import com.jrecord.record.audio.IAudioEncoderListener;
import com.jrecord.record.audio.IAudioRecorderListener;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startAudioRecord(View view) {
        AudioConfig config = new AudioConfig();
        config.bitRate(128000);
        AudioEncoder.getInstance().addListener(TAG, new IAudioEncoderListener() {
            @Override
            public void onAudioEncodeStart(AudioConfig config) {
                ILog.d(TAG, "onAudioEncodeStart");
            }

            @Override
            public void onAudioEncodeFormat(MediaFormat format) {
                ILog.d(TAG, "onAudioEncodeFormat : " + format.getString(MediaFormat.KEY_MIME));
            }

            @Override
            public void onAudioEncodeFrame(int flags, long durationNs, byte[] buffer) {
                ILog.d(TAG, "onAudioEncodeFrame : flags = " + flags+", durationNs = " + durationNs/1000 +", buffer = " + buffer.length);
            }

            @Override
            public void onAudioEncodeStop() {
                ILog.d(TAG, "onAudioEncodeStop");
            }
        });
        AudioRecorder.getInstance().addListener(TAG, new IAudioRecorderListener() {
            @Override
            public void onAudioRecordStart(AudioConfig config) {
                ILog.d(TAG, "onAudioRecordStart");
                AudioEncoder.getInstance().init(config).start();
            }

            @Override
            public void onAudioRecordFrame(long durationNs, byte[] buffer, boolean endOfStream) {
                ILog.d(TAG, "onAudioRecordFrame : durationNs = " + durationNs +", buffer = " + buffer.length+", endOfStream = " + endOfStream);
                AudioEncoder.getInstance().encodePCM(durationNs, buffer, endOfStream);
            }

            @Override
            public void onAudioRecordStop() {
                ILog.d(TAG, "onAudioRecordStop");
                AudioEncoder.getInstance().stop();
            }
        });

        AudioRecorder.getInstance().init(config).start();
    }

    public void stopAudioRecord(View view) {
        AudioRecorder.getInstance().stop();
    }

    @NonNull
    @Override
    protected String[] needPermissions() {
        String[] permissions = new String[2];
        System.arraycopy(DEFAULT_PERMISSION, 0, permissions, 0, 1);
        permissions[1] = android.Manifest.permission.RECORD_AUDIO;
        return permissions;
    }
}
