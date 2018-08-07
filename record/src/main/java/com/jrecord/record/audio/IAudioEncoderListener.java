package com.jrecord.record.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;

public interface IAudioEncoderListener {
    void onAudioEncodeStart(AudioConfig config);
    void onAudioEncodeFormat(MediaFormat format);
    void onAudioEncodeFrame(MediaCodec.BufferInfo info, byte[] buffer);
    void onAudioEncodeStop();
}
