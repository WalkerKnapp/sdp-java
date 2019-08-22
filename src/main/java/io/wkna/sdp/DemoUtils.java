package io.wkna.sdp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

public class DemoUtils {

    public static int getOffset(long offset, int startPosition) {
        return offset >= startPosition ? (int) (offset - startPosition) : 0;
    }

    public static long getOffsetLong(long offset, int startPosition) {
        return offset >= startPosition ? (offset - startPosition) : 0;
    }

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

    public static int writeInt(int value, ByteBuffer buffer, int remaining, int offset) {
        int bytesToRead = 4 - offset;
        if(remaining < bytesToRead) {
            bytesToRead = remaining;
        }

        for(int i = offset; i < bytesToRead + offset; i++) {
            byte b = (byte)((value >> (i * 8)) & 0xFF);
            buffer.put(b);
        }

        return bytesToRead;
    }

    public static String readString(SeekableByteChannel sbc, int size) throws IOException {
        byte[] rawBuffer = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
        sbc.read(buffer);
        return new String(rawBuffer);
    }

    public static int writeString(String value, ByteBuffer buffer, int remaining, int paddedSize, int offset) {
        byte[] raw = value.getBytes();

        if(raw.length > paddedSize) {
            throw new IllegalArgumentException("Length of string cannot be larger than paddedSize");
        }

        int read = remaining >= (paddedSize - offset) ? (paddedSize - offset) : remaining;
        if((raw.length - offset) >= read) {
            buffer.put(raw, offset, read);
        } else {
            buffer.put(raw, offset, raw.length - offset);
            for(int i = raw.length - offset; i < read; i++) {
                buffer.put((byte) 0x00);
            }
        }
        return read;
    }

    public static int writeRawData(byte[] data, ByteBuffer dst, int remaining, int offset) {
        if (remaining < data.length - offset) {
            dst.put(data, offset, remaining);
            return remaining;
        } else {
            dst.put(data, offset, data.length - offset);
            return data.length - offset;
        }
    }

    public static float readFloat(SeekableByteChannel sbc) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        sbc.read(buffer);
        return buffer.getFloat(0);
    }

    public static int writeFloat(float value, ByteBuffer buffer, int remaining, int offset) {
        return writeInt(Float.floatToIntBits(value), buffer, remaining, offset);
    }

    public static float[] readFloatArray(SeekableByteChannel sbc, int size) throws IOException {
        float[] arr = new float[size];
        for(int i = 0; i < size; i++){
            arr[i] = readFloat(sbc);
        }
        return arr;
    }

    public static int writeFloatArray(float[] value, ByteBuffer buffer, int remaining, int offset) {
        int totalBytesWritten = 0;

        for(int i = offset/4; i < value.length; i++) {
            if(i == offset/4) {
                int rawOffset = offset % 4;
                totalBytesWritten += writeFloat(value[i], buffer, remaining - totalBytesWritten, rawOffset);
            } else {
                totalBytesWritten += writeFloat(value[i], buffer, remaining - totalBytesWritten, 0);
            }

            if(remaining - totalBytesWritten == 0) {
                return totalBytesWritten;
            }
        }

        return totalBytesWritten;
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
