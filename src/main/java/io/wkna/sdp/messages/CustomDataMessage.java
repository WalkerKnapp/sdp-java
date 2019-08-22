package io.wkna.sdp.messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

public class CustomDataMessage extends DemoMessage {
    //TODO: Research the purpose of this
    private int unknown;
    private int size;
    private byte[] data;

    public CustomDataMessage(SeekableByteChannel sbc, boolean alignmentByte) throws IOException {
        super(sbc, alignmentByte);
        unknown = readInt(sbc);
        //System.out.println("\t\tunknown: " + unknown);
        size = readInt(sbc);
        //System.out.println("\t\tsize: " + size);
        data = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        sbc.read(buffer);
        //System.out.println("\t\tdata: " + new String(data));
    }

    @Override
    long getDataSize() {
        return 4+4+size;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, long offset) {
        int totalWrites = 0;

        if(offset < 4) {
            totalWrites += writeInt(unknown, dst, remaining - totalWrites, (int) offset);
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(offset < 4+4) {
            totalWrites += writeInt(size, dst, remaining - totalWrites, getOffset(offset, 4));
            if (totalWrites == remaining) {
                return totalWrites;
            }
        }

        if (offset < 4 + 4 + size) {
            totalWrites += writeRawData(data, dst, remaining - totalWrites, getOffset(offset, 4+4));
            return totalWrites;
        }

        throw new IllegalArgumentException("CustomData Message offset must fall on a value boundary.");
    }

    @Override
    public byte getMessageType() {
        return 8;
    }
}
