package io.wkna.sdp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

public class DemoUtils {

    public static int writeByte(byte value, ByteBuffer buffer, int remaining) {
        if(remaining >= 1) {
            buffer.put(value);
            return 1;
        } else {
            return 0;
        }
    }

    public static int readInt(SeekableByteChannel sbc) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        sbc.read(buffer);
        return buffer.getInt(0);
    }

    public static int writeInt(int value, ByteBuffer buffer, int remaining) {
        if(remaining >= 4) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(value);
            return 4;
        } else {
            return 0;
        }
    }

    public static String readString(SeekableByteChannel sbc, int size) throws IOException {
        byte[] rawBuffer = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
        sbc.read(buffer);
        return new String(rawBuffer);
    }

    public static int writeString(String value, ByteBuffer buffer, int remaining, int paddedSize) {
        byte[] raw = value.getBytes();

        if(raw.length > paddedSize) {
            throw new IllegalArgumentException("Length of string cannot be larger than paddedSize");
        }

        if(remaining >= raw.length) {
            buffer.put(raw);
            if(raw.length < paddedSize) {
                for(int i = raw.length; i < paddedSize; i++) {
                    buffer.put((byte) 0);
                }
            }
            return paddedSize;
        } else {
            return 0;
        }
    }

    public static float readFloat(SeekableByteChannel sbc) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        sbc.read(buffer);
        return buffer.getFloat(0);
    }

    public static int writeFloat(float value, ByteBuffer buffer, int remaining) {
        if(remaining >= 4) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putFloat(value);
            return 4;
        } else {
            return 0;
        }
    }

    public static float[] readFloatArray(SeekableByteChannel sbc, int size) throws IOException {
        float[] arr = new float[size];
        for(int i = 0; i < size; i++){
            arr[i] = readFloat(sbc);
        }
        return arr;
    }

    public static int writeFloatArray(float[] value, ByteBuffer buffer, int remaining) {
        if(remaining >= value.length * 4) {
            for (float v : value) {
                buffer.putFloat(v);
            }
            return value.length * 4;
        } else {
            return 0;
        }
    }

    public static String floatsToString(float[] arr){
        StringBuilder sb = new StringBuilder("[");
        for(int i = 0; i < arr.length; i++){
            if(i != 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
