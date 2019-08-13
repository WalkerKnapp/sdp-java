package io.wkna.sdp.messages;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

public class DataTablesMessage extends DemoMessage {
    private int size;
    // TODO: Objectify this
    private byte[] data;
    public DataTablesMessage(SeekableByteChannel sbc, boolean alignmentByte) throws IOException {
        super(sbc, alignmentByte);
        size = readInt(sbc);
        System.out.println("\t\tsize: " + size);
        data = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        sbc.read(buffer);
        System.out.println("\t\tdata: " + HexBin.encode(data));
    }

    @Override
    long getDataSize() {
        return 4 + size;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, int offset) {
        int writeSize;
        int totalWrites = 0;

        if (offset == 0) {
            if ((writeSize = writeInt(size, dst, remaining - totalWrites)) == 0) {
                return totalWrites;
            }
            totalWrites += writeSize;

            if (remaining - totalWrites < data.length) {
                dst.put(data, 0, remaining - totalWrites);
                return remaining;
            } else {
                dst.put(data, 0, data.length);
                return data.length;
            }
        }
        if (offset >= 4 && offset <= 4 + size) {
            if ((remaining - totalWrites) != 0) {
                int dataOffset = offset - 4;

                if (remaining - totalWrites < data.length - dataOffset) {
                    dst.put(data, dataOffset, remaining - totalWrites);
                    return remaining;
                } else {
                    dst.put(data, dataOffset, data.length - dataOffset);
                    return data.length - dataOffset;
                }
            } else {
                return 0;
            }
        } else {
            throw new IllegalArgumentException("ConsoleCmd Message offset must fall on a value boundary.");
        }
    }

    @Override
    public byte getMessageType() {
        return 6;
    }
}
