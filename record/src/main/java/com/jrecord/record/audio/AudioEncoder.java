package com.jrecord.record.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.Pools;
import android.text.TextUtils;

import com.jrecord.common.AppUtils;
import com.jrecord.common.ILog;
import com.jrecord.record.audio.data.PCMData;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 音频数据编码线程
 */
public class AudioEncoder implements Runnable {

    private static final String TAG = "AudioEncoder";
    private AudioEncoder(){}
    private static AudioEncoder INSTANCE = new AudioEncoder();
    public static AudioEncoder getInstance() {
        return INSTANCE;
    }


    /**
     * 音频数据缓存池, 避免无限制创建byte数组
     */
    private Pools.SynchronizedPool<byte[]> mBufferPool = new Pools.SynchronizedPool<byte[]>(10);
    private byte[] acquireBuffer(int bufferSize) {
        byte[] buffer = mBufferPool.acquire();
        if (null == buffer || buffer.length != bufferSize) {
            buffer = new byte[bufferSize];
        }
        return buffer;
    }
    private void releaseBuffer(byte[] buffer) {
        if (null == buffer) {
            return;
        }
        try {
            Arrays.fill(buffer, (byte) 0);
            mBufferPool.release(buffer);
        } catch (Exception e) {
            ILog.e(TAG, "releaseBuffer", e);
        }
    }

    private final Object mFence = new Object();
    private AudioConfig mConfig;

    public AudioEncoder init(AudioConfig config) {
        mConfig = config;
        return this;
    }

    // 当前线程运行状态
    private boolean mRunning = false;
    // 标记当前编码状态
    private boolean mEncoding = false;
    // 标记当前停止状态
    private boolean mStop = true;

    private MediaCodec mEncoder;

    private byte[] mAACData;
    /**
     * 开始编码
     * @return
     */
    public boolean start() {
        if (null == mConfig) {
            mConfig = new AudioConfig();
        }
        synchronized (mFence) {
            if (mRunning) {
                ILog.w(TAG, "AudioEncoder is Running");
                return false;
            }

            mRunning = true;
        }
        ILog.v(TAG, "AudioEncoder start");
        new Thread(AudioConfig.AUDIO_GROUP, this, TAG).start();
        return true;
    }

    /**
     * 停止编码
     * @return
     */
    public boolean stop() {
        synchronized (mFence) {
            if (mStop) {
                ILog.w(TAG, "AudioEncoder is Stopped");
                return false;
            }
            mStop = true;
        }
        Message message = mHandler.obtainMessage(WHAT_STOP);
        message.sendToTarget();
        ILog.v(TAG, "AudioEncoder stop");
        return true;
    }

    /**
     * 是否编码中
     * @return
     */
    public boolean isEncoding() {
        synchronized (mFence) {
            return mEncoding;
        }
    }
    private Pools.SynchronizedPool<PCMData> mPCMPool = new Pools.SynchronizedPool<PCMData>(10);
    private PCMData acquirePCMData() {
        PCMData pcmData = mPCMPool.acquire();
        if (null == pcmData) {
            pcmData = new PCMData();
        }
        return pcmData;
    }
    private void releasePCMData(PCMData data) {
        if (null == data) {
            return;
        }
        data.endOfStream = false;
        data.durationNs = 0;
        data.data = null;
        mPCMPool.release(data);
    }

    private MediaCodec.BufferInfo mOutputInfo = new MediaCodec.BufferInfo();
    /**
     * 编码PCM数据
     * @param durationNs 原始PCM数据的时长
     * @param pcm PCM源数据
     * @param endOfStream 是否是结尾帧
     */
    public void encodePCM(long durationNs, byte[] pcm, boolean endOfStream) {
        // 编码器没有启动
        if (!isEncoding()) {
            ILog.w(TAG, "AudioEncoder is not Encoding");
            return;
        }
        // 停止也不再进行编码
        synchronized (mFence) {
            if (mStop) {
                ILog.w(TAG, "AudioEncoder is Stopped");
                return;
            }
        }
        PCMData data = acquirePCMData();
        data.durationNs = durationNs;
        data.data = pcm;
        data.endOfStream = endOfStream;
        Message message = mHandler.obtainMessage(WHAT_ENCODE);
        message.obj = data;
        message.sendToTarget();

        // 如果是结束帧, 就停止编码线程
        if (endOfStream) {
            stop();
        }
    }
    private void _encodePCM(long durationNs, byte[] pcm, boolean endOfStream) {
        // 记录输入输出缓存index, 以便及时释放
        int inputIndex = -1;
        int outputIndex = -1;

        try {
            // Par1: Encode
            // 获取编码器输入缓冲块，timeoutUs为0表示立刻返回, 没有就返回-1
            inputIndex = mEncoder.dequeueInputBuffer(0);
            if (inputIndex < 0) {
                return;
            }
            int len = pcm.length;
            // 拷贝原始数据PCM，不污染源数据
            byte[] data = acquireBuffer(len);
            System.arraycopy(pcm, 0, data, 0, len);

            // 把原始PCM塞入编码器的inputBuffer
            ByteBuffer[] inputBuffer = mEncoder.getInputBuffers();
            ByteBuffer buffer = inputBuffer[inputIndex];
            buffer.clear();
            buffer.put(data);

            // 添加到编码器编码
            int flag = endOfStream ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0;
            mEncoder.queueInputBuffer(inputIndex, 0, data.length, durationNs / 1000, flag);

            // 释放编码前拷贝的原始资源
            releaseBuffer(data);

            // Part2: 获取编码后的AAC数据
            while (true) {
                // 获取输出缓冲块
                int res = mEncoder.dequeueOutputBuffer(mOutputInfo, 0);

                if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat outputFormat = mEncoder.getOutputFormat();
                    onAudioEncodeFormat(outputFormat);
                } else if (res == MediaCodec.INFO_TRY_AGAIN_LATER){
                    // 如果不是结束标记，就等待
                    if (!endOfStream) {
                        break;
                    }
                } else if (res >= 0) {
                    // 获取编码好的AAC数据
                    ByteBuffer[] outputBuffer = mEncoder.getOutputBuffers();
                    outputIndex = res;
                    buffer = outputBuffer[outputIndex];
                    int capacity = buffer.capacity();
                    int dataLen = buffer.remaining();
                    if (null == mAACData || mAACData.length != capacity) {
                        mAACData = new byte[capacity];
                    }
                    buffer.get(mAACData, mOutputInfo.offset, dataLen);
                    long presentationTimeUs = mOutputInfo.presentationTimeUs;
                    int flags = mOutputInfo.flags;
                    // 返回编码好的aac数据，注意这个回调跟pcm数据一样，使用的时候拷贝源数据,
                    // 不要直接使用，在下次更新这个数据的时候，会把这个数据污染掉
                    onAudioEncodeFrame(flags, presentationTimeUs, mAACData);
                    mEncoder.releaseOutputBuffer(outputIndex, false);
                    if ((flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if(!endOfStream) {
                            ILog.w(TAG, "AudioEncoderCore.drainEncoder : reached end of stream unexpectedly");
                        } else {
                            ILog.d(TAG, "AudioEncoderCore.drainEncoder : end of stream reached");
                        }
                        break;
                    }
                }
            }

        } catch (Exception e) {
            ILog.e(TAG, "encodePCM", e);
            if (outputIndex >= 0) {
                try {
                    mEncoder.releaseOutputBuffer(outputIndex, false);
                } catch (Exception e1) {}
            }
        }

    }

    /*-------------- 接口 --------------*/
    private Map<String, IAudioEncoderListener> mListeners = new LinkedHashMap<>();

    public void addListener(String key, IAudioEncoderListener listener) {
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

    private void onAudioEncodeStart(AudioConfig config) {
        Set<String> keys = mListeners.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IAudioEncoderListener listener = mListeners.get(key);
            if (null == listener) {
                iterator.remove();
                continue;
            }

            listener.onAudioEncodeStart(config);
        }
    }

    private void onAudioEncodeFormat(MediaFormat format) {
        Set<String> keys = mListeners.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IAudioEncoderListener listener = mListeners.get(key);
            if (null == listener) {
                iterator.remove();
                continue;
            }

            listener.onAudioEncodeFormat(format);
        }
    }

    private void onAudioEncodeFrame(int flags, long presentationTimeUs, byte[] buffer) {
        Set<String> keys = mListeners.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IAudioEncoderListener listener = mListeners.get(key);
            if (null == listener) {
                iterator.remove();
                continue;
            }

            listener.onAudioEncodeFrame(flags, presentationTimeUs * 1000, buffer);
        }
    }


    private void onAudioEncodeStop() {
        Set<String> keys = mListeners.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            IAudioEncoderListener listener = mListeners.get(key);
            if (null == listener) {
                iterator.remove();
                continue;
            }

            listener.onAudioEncodeStop();
        }
    }

    private EncoderHandler mHandler;
    @Override
    public void run() {
        ILog.v(TAG, "AudioEncoder thread start");
        try {
            Looper.prepare();
            mHandler = new EncoderHandler(this);
            synchronized (mFence) {
                mEncoding = false;
                mStop = false;
            }

            // 开始编码
            int sampleRateInHz = mConfig.sampleRateInHz(); // 44100
            int channelConfig = mConfig.channelConfig(); // 双声道
            int audioFormat = mConfig.audioFormat(); // pcm16
            int bytesPerFrame = mConfig.getBytesPerFrame(); // 每一帧字节数 pcm16(2byte)
            int channelCount = mConfig.getChannelCount();
            int bitRate = mConfig.bitRate();

            String mine = mConfig.mime();
            MediaFormat format = MediaFormat.createAudioFormat(mine, sampleRateInHz, channelCount);
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);

            mEncoder = MediaCodec.createEncoderByType(mine);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncoder.start();
            synchronized (mFence) {
                mEncoding = true;
            }

            onAudioEncodeStart(mConfig);
            Looper.loop();
        } catch (Exception e) {
            ILog.e(TAG, "RUN Error", e);
        } finally {
            stopEncoding();
        }

        synchronized (mFence) {
            mStop = true;
            mEncoding = false;
            mRunning = false;
        }

        onAudioEncodeStop();
        ILog.v(TAG, "AudioEncoder thread stopped");
        mListeners.values().clear();
        mListeners.clear();
        mConfig = null;
        // 清空同步池中的资源
        do {
            mBufferPool.acquire();
        } while (mBufferPool.acquire() != null);
        PCMData data;
        while ((data = mPCMPool.acquire()) != null) {
            data.data = null;
            data.durationNs = 0;
            data.endOfStream = false;
        }
        mAACData = null;
        mHandler = null;
    }

    private void stopEncoding() {
        if (null != mEncoder) {
            mEncoder.stop();
            mEncoder.release();
            // 释放MediaCodec对当前线程的Looper引用
            AppUtils.setNull(mEncoder, "mCallbackHandler");
            AppUtils.setNull(mEncoder, "mOnFrameRenderedHandler");
            AppUtils.setNull(mEncoder, "mEventHandler");
            mEncoder = null;
        }
    }

    private static final int WHAT_ENCODE = 1;
    private static final int WHAT_STOP = 2;
    private static class EncoderHandler extends Handler {
        private WeakReference<AudioEncoder> mRef;

        public EncoderHandler(AudioEncoder encoder) {
            this.mRef = new WeakReference<>(encoder);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AudioEncoder encoder = mRef.get();
            if (null == encoder) {
                return;
            }

            int what = msg.what;
            if (what == WHAT_ENCODE) {
                PCMData pcm = (PCMData) msg.obj;
                encoder._encodePCM(pcm.durationNs, pcm.data, pcm.endOfStream);
                encoder.releasePCMData(pcm);
            } else if (what == WHAT_STOP) {
                mRef.clear();
                encoder.stopEncoding();
                Looper myLooper = Looper.myLooper();
                if (null != myLooper) {
                    myLooper.quit();
                }
            }
        }
    }


}
