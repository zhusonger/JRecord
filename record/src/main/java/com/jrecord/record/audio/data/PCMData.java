package com.jrecord.record.audio.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * PCM 数据结构
 */
public class PCMData implements Parcelable {
    public long durationNs;
    public byte[] data;
    public boolean endOfStream;

    public PCMData() {
    }

    public PCMData(long durationNs, byte[] data, boolean endOfStream) {
        this.durationNs = durationNs;
        this.data = data;
        this.endOfStream = endOfStream;
    }


    protected PCMData(Parcel in) {
        durationNs = in.readLong();
        data = in.createByteArray();
        endOfStream = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(durationNs);
        dest.writeByteArray(data);
        dest.writeByte((byte) (endOfStream ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PCMData> CREATOR = new Creator<PCMData>() {
        @Override
        public PCMData createFromParcel(Parcel in) {
            return new PCMData(in);
        }

        @Override
        public PCMData[] newArray(int size) {
            return new PCMData[size];
        }
    };

    @Override
    public String toString() {
        return "PCMData{" +
                "durationNs=" + durationNs +
                ", data=" + Arrays.toString(data) +
                ", endOfStream=" + endOfStream +
                '}';
    }
}