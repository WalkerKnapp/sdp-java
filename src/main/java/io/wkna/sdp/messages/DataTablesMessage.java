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
        //System.out.println("\t\tsize: " + size);
        data = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        sbc.read(buffer);
        //System.out.println("\t\tdata: " + HexBin.encode(data));
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
            totalWrites += writeRawData(data, dst, remaining - totalWrites, getOffset(offset, 4));
            return totalWrites;
        }

        throw new IllegalArgumentException("ConsoleCmd Message offset must fall on a value boundary.");
    }

    @Override
    public byte getMessageType() {
        return 6;
    }
}
