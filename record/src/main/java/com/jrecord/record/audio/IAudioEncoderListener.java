package com.jrecord.record.audio;

import android.media.MediaFormat;

public interface IAudioEncoderListener {
    void onAudioEncodeStart(AudioConfig config);
    void onAudioEncodeFormat(MediaFormat format);
    void onAudioEncodeFrame(int flags, long durationNs, byte[] buffer);
    void onAudioEncodeStop();
}
