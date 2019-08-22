package io.wkna.sdp.messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

public class ConsoleCmdMessage extends DemoMessage {
    private int size;
    private String data;
    private byte[] rawBuffer;

    public ConsoleCmdMessage(SeekableByteChannel sbc, boolean alignmentByte) throws IOException {
        super(sbc, alignmentByte);
        size = readInt(sbc);
        //System.out.println("\t\tsize: " + size);
        rawBuffer = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
        sbc.read(buffer);
        data = new String(rawBuffer);
        //System.out.println("\t\tdata: " + data);
    }

    @Override
    long getDataSize() {
        return 4 + size;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, long offset) {
        int totalWrites = 0;

        if(offset < 4) {
            totalWrites += writeInt(size, dst, remaining - totalWrites, (int) offset);
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(offset < 4+size) {
            totalWrites += writeRawData(rawBuffer, dst, remaining - totalWrites, getOffset(offset, 4));
            return totalWrites;
        }
        /*if(offset < 4+size) {
            totalWrites += writeString(data, dst, remaining - totalWrites, size, getOffset(offset, 4));
            return totalWrites;
        }*/
        throw new IllegalArgumentException("ConsoleCmd Message offset must fall on a value boundary.");
    }

    @Override
    public byte getMessageType() {
        return 4;
    }
}
