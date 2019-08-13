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
        System.out.println("\t\tunknown: " + unknown);
        size = readInt(sbc);
        System.out.println("\t\tsize: " + size);
        data = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        sbc.read(buffer);
        System.out.println("\t\tdata: " + new String(data));
    }

    @Override
    long getDataSize() {
        return 4+4+size;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, int offset) {
        int writeSize;
        int totalWrites = 0;

        switch (offset) {
            case 0:
                if ((writeSize = writeInt(unknown, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4:
                if ((writeSize = writeInt(size, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4+4:
                if(remaining - totalWrites >= size) {
                    dst.put(data);
                    totalWrites += size;
                }
                return totalWrites;
            default:
                throw new IllegalArgumentException("CustomData Message offset must fall on a value boundary.");
        }
    }

    @Override
    public byte getMessageType() {
        return 8;
    }
}
