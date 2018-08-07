package com.jrecord.record.audio;

import android.media.AudioRecord;
import android.os.Looper;
import android.text.TextUtils;

import com.jrecord.common.AppUtils;
import com.jrecord.common.ILog;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 音频录制线程,使用系统自带的AudioRecord的录制线程
 * 其他音频录制另外新建线程来实现录制功能
 */
public class AudioRecorder implements Runnable {
    private static final String TAG = "AudioRecorder";

    private final Object mFence = new Object();
    private AudioRecord mRecord;
    private AudioConfig mConfig;

    private AudioRecorder(){}
    private static AudioRecorder INSTANCE = new AudioRecorder();
    public static AudioRecorder getInstance() {
        return INSTANCE;
    }

    public AudioRecorder init(AudioConfig config) {
        mConfig = config;
        return this;
    }

    // 当前线程运行状态
    private boolean mRunning = false;
    // 标记当前录制状态
    private boolean mRecording = false;
    // 标记当前停止状态
    private boolean mStop = true;
    // 读取PCM数据的buffer
    private byte[] mPCMBuffer;

    /**
     * 开始录音
     * @return
     */
    public boolean start() {
        if (null == mConfig) {
            mConfig = new AudioConfig();
        }
        synchronized (mFence) {
            if (mRunning) {
                ILog.w(TAG, "AudioRecorder is Running");
                return false;
            }

            mRunning = true;
        }
        ILog.v(TAG, "AudioRecorder start");
        new Thread(AudioConfig.AUDIO_GROUP, this, TAG).start();
        return true;
    }

    /**
     * 停止录音
     * @return
     */
    public boolean stop() {
        synchronized (mFence) {
            if (mStop) {
                ILog.w(TAG, "AudioRecorder is Stopped");
                return false;
            }
            mStop = true;
        }
        ILog.v(TAG, "AudioRecorder stop");
        return true;
    }

    /**
     * 是否录音中
     * @return
     */
    public boolean isRecording() {
        synchronized (mFence) {
            return mRecording;
        }
    }

    /*-------------- 接口 --------------*/
    private Map<String, IAudioRecorderListener> mListeners = new LinkedHashMap<>();

    public void addListener(String key, IAudioRecorderListener listener) {
        if (TextUtils.isEmpty(key)) {
            key = TAG;
        }
        mListeners.put(key, listener);
    }
    public void removeListener(String key) {
        if (TextUtils.isEmpty(key)) {
            key = TAG;
        }
        mListeners.put(key, null);
    }

    private void onAudioRecordStart(AudioConfig config) {
        Set<String> keys = mListeners.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IAudioRecorderListener listener = mListeners.get(key);
            if (null == listener) {
                iterator.remove();
                continue;
            }

            listener.onAudioRecordStart(config);
        }
    }

    private void onAudioRecordFrame(long durationNs, byte[] buffer, boolean endOfStream) {
        Set<String> keys = mListeners.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IAudioRecorderListener listener = mListeners.get(key);
            if (null == listener) {
                iterator.remove();
                continue;
            }

            listener.onAudioRecordFrame(durationNs, buffer, endOfStream);
        }
    }


    private void onAudioRecordStop() {
        Set<String> keys = mListeners.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IAudioRecorderListener listener = mListeners.get(key);
            if (null == listener) {
                iterator.remove();
                continue;
            }

            listener.onAudioRecordStop();
        }
    }

    @Override
    public void run() {
        // 设置优先级
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        ILog.v(TAG, "AudioRecorder thread start");
        try {
            Looper.prepare();
            synchronized (mFence) {
                mRecording = false;
                mStop = false;
            }
            // 开始录音
            int audioSource = mConfig.audioSource(); // mic
            int sampleRateInHz = mConfig.sampleRateInHz(); // 44100
            int channelConfig = mConfig.channelConfig(); // 双声道
            int audioFormat = mConfig.audioFormat(); // pcm16
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            // 每一帧字节数组
            int bufferSizeInBytes = mConfig.bufferSizeInBytes();
            if (minBufferSize > bufferSizeInBytes) {
                bufferSizeInBytes = (int) Math.ceil((float) minBufferSize / AudioConfig.UNIT_BUFFER_SIZE_IN_BYTES) * AudioConfig.UNIT_BUFFER_SIZE_IN_BYTES;
            }
            mConfig.bufferSizeInBytes(bufferSizeInBytes);
            //
            int bytesPerFrame = mConfig.getBytesPerFrame() * mConfig.getChannelCount(); // 每一帧字节数 pcm16(2byte) * 声道数
            int frameSize = bufferSizeInBytes / bytesPerFrame; // 每个缓冲区存储的帧数
            mPCMBuffer = new byte[bufferSizeInBytes];
            mRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

            ILog.d(TAG, "bufferSizeInBytes = " + bufferSizeInBytes);
            mRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
                // 记录开始录音的时间
                long startTime = -1; // ns


                @Override
                public void onMarkerReached(AudioRecord recorder) {}

                @Override
                public void onPeriodicNotification(AudioRecord recorder) {
                    // 更新开始录制时间戳
                    long now = System.nanoTime();
                    if (startTime < 0) {
                        ILog.v(TAG, "onPeriodicNotification start");
                        startTime = now;
                    }
                    // 录音时长
                    long durationNs = now - startTime;
                    // 读取录音数据, 必须读取, 否则后续的录音数据无法压入到AudioRecord中
                    int ret = recorder.read(mPCMBuffer, 0, mPCMBuffer.length);
                    // 音频读取失败
                    if (ret == AudioRecord.ERROR_INVALID_OPERATION
                            || ret == AudioRecord.ERROR_BAD_VALUE
                            || ret == AudioRecord.ERROR_DEAD_OBJECT) {
                        ILog.e(TAG, "read error : " + ret);
                        return;
                    }
                    synchronized (mFence) {
                        // 停止录制
                        if (mStop) {
                            onAudioRecordFrame(durationNs, mPCMBuffer, true);
                            stopRecord();
                            Looper myLooper = Looper.myLooper();
                            if (null != myLooper) {
                                myLooper.quit();
                            }
                            ILog.d(TAG, "onPeriodicNotification stop");
                            return;
                        }
                    }

                    // 返回录制的录音数据，注意使用的地方需要拷贝，不能直接使用，否则会引起音频数据不正确, 表现千奇百怪
                    // 一般在这步进行编码
                    onAudioRecordFrame(durationNs, mPCMBuffer, false);
                }
            });
            mRecord.setPositionNotificationPeriod(frameSize);
            mRecord.startRecording();
            synchronized (mFence) {
                mRecording = true;
            }

            // 回调开始接口
            onAudioRecordStart(mConfig);

            Looper.loop();
        } catch (Exception e) {
          ILog.e(TAG, "RUN Error", e);
        } finally {
            stopRecord();
        }
        synchronized (mFence) {
            mStop = true;
            mRecording = false;
            mRunning = false;
        }

        onAudioRecordStop();

        // 清除回调的引用
        mListeners.clear();
        mConfig = null;
        mPCMBuffer = null;
        ILog.v(TAG, "AudioRecorder thread stopped");
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        if (null != mRecord) {
            try {
                mRecord.setRecordPositionUpdateListener(null);
                mRecord.stop();
                mRecord.release();
                // 移除setRecordPositionUpdateListener导致mInitializationLooper引用的线程Looper，但是没有释放的泄漏
                AppUtils.setNull(mRecord, "mInitializationLooper");
                mRecord = null;
            } catch (Exception e) {
                ILog.e(TAG, "stop record", e);
            }
        }
    }
}
