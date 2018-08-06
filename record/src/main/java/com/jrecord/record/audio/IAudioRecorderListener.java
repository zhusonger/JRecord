package com.jrecord.record.audio;

public interface IAudioRecorderListener {
    void onAudioRecordStart(AudioConfig config);
    void onAudioRecordFrame(long durationNs, byte[] buffer, boolean endOfStream);
    void onAudioRecordStop();
}
