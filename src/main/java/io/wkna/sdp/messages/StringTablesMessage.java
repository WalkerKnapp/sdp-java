package io.wkna.sdp.messages;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

public class StringTablesMessage extends DemoMessage {
    private int size;
    //TODO: Objectify this
    private byte[] tableData;

    private boolean newEngine;

    public StringTablesMessage(SeekableByteChannel sbc, boolean alignmentByte, boolean newEngine) throws IOException {
        super(sbc, alignmentByte);
        size = readInt(sbc);
        //System.out.println("\t\tsize: " + size);
        tableData = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(tableData);
        sbc.read(buffer);
        //System.out.println("\t\ttableData: " + HexBin.encode(tableData));

        this.newEngine = newEngine;
    }

    @Override
    long getDataSize() {
        return 4+size;
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
            totalWrites += writeRawData(tableData, dst, remaining - totalWrites, getOffset(offset, 4));
            return totalWrites;
        }

        throw new IllegalArgumentException("StringTables Message offset must fall on a value boundary.");
    }

    @Override
    public byte getMessageType() {
        return (byte) (newEngine ? 9 : 8);
    }
}
