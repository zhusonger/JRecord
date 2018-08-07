package com.jrecord;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioButton;

import com.jrecord.common.ILog;
import com.jrecord.common.base.BaseActivity;
import com.jrecord.record.audio.AudioConfig;
import com.jrecord.record.audio.AudioEncoder;
import com.jrecord.record.audio.AudioRecorder;
import com.jrecord.record.audio.IAudioEncoderListener;
import com.jrecord.record.audio.IAudioRecorderListener;

import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends BaseActivity {

    private MediaMuxer muxer;
    private int audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startAudioRecord(View view) {
        try {
            muxer = new MediaMuxer(Environment.getExternalStorageDirectory()+"/jrecord.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        AudioConfig config = new AudioConfig();
        RadioButton stereoRb = findViewById(R.id.rb_stereo);
        if (stereoRb.isChecked()) {
            config.channelConfig(AudioFormat.CHANNEL_IN_STEREO);
        } else {
            config.channelConfig(AudioFormat.CHANNEL_IN_MONO);
        }
        config.bitRate(128000);
        AudioEncoder.getInstance().addListener(TAG, new IAudioEncoderListener() {
            @Override
            public void onAudioEncodeStart(AudioConfig config) {
                ILog.d(TAG, "onAudioEncodeStart");
            }

            @Override
            public void onAudioEncodeFormat(MediaFormat format) {
                ILog.d(TAG, "onAudioEncodeFormat : " + format.getString(MediaFormat.KEY_MIME));
                audioTrack = muxer.addTrack(format);
                muxer.start();
            }

            @Override
            public void onAudioEncodeFrame(MediaCodec.BufferInfo info, byte[] buffer) {
//                ILog.d(TAG, "onAudioEncodeFrame : flags = " + flags+", durationNs = " + durationNs +", buffer = " + buffer.length);
                muxer.writeSampleData(audioTrack, ByteBuffer.wrap(buffer), info);
            }

            @Override
            public void onAudioEncodeStop() {
                ILog.d(TAG, "onAudioEncodeStop");
                muxer.stop();
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
//                ILog.d(TAG, "onAudioRecordFrame : durationNs = " + durationNs/1000000 +", buffer = " + buffer.length+", endOfStream = " + endOfStream);
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

    public void playAudioRecord(View view) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(Environment.getExternalStorageDirectory()+"/jrecord.mp4");
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
