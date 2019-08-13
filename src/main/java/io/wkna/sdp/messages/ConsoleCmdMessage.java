package io.wkna.sdp.messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

public class ConsoleCmdMessage extends DemoMessage {
    private int size;
    private String data;

    public ConsoleCmdMessage(SeekableByteChannel sbc, boolean alignmentByte) throws IOException {
        super(sbc, alignmentByte);
        size = readInt(sbc);
        System.out.println("\t\tsize: " + size);
        byte[] rawBuffer = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
        sbc.read(buffer);
        data = new String(rawBuffer);
        System.out.println("\t\tdata: " + data);
    }

    @Override
    long getDataSize() {
        return 4 + size;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, int offset) {
        int writeSize;
        int totalWrites = 0;

        switch (offset) {
            case 0:
                if ((writeSize = writeInt(size, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4:
                if ((writeSize = writeString(data, dst, remaining - totalWrites, size)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
                return totalWrites;
            default:
                throw new IllegalArgumentException("ConsoleCmd Message offset must fall on a value boundary.");
        }
    }

    @Override
    public byte getMessageType() {
        return 4;
    }
}
