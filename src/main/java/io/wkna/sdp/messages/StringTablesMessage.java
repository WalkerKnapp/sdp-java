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
        System.out.println("\t\tsize: " + size);
        tableData = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(tableData);
        sbc.read(buffer);
        System.out.println("\t\ttableData: " + HexBin.encode(tableData));

        this.newEngine = newEngine;
    }

    @Override
    long getDataSize() {
        return 4+size;
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

            if (remaining - totalWrites < tableData.length) {
                dst.put(tableData, 0, remaining - totalWrites);
                return remaining;
            } else {
                dst.put(tableData, 0, tableData.length);
                return tableData.length;
            }
        }
        if (offset >= 4 && offset <= 4 + size) {
            if ((remaining - totalWrites) != 0) {
                int dataOffset = offset - 4;

                if (remaining - totalWrites < tableData.length - dataOffset) {
                    dst.put(tableData, dataOffset, remaining - totalWrites);
                    return remaining;
                } else {
                    dst.put(tableData, dataOffset, tableData.length - dataOffset);
                    return tableData.length - dataOffset;
                }
            } else {
                return 0;
            }
        } else {
            throw new IllegalArgumentException("StringTables Message offset must fall on a value boundary.");
        }
    }

    @Override
    public byte getMessageType() {
        return (byte) (newEngine ? 9 : 8);
    }
}
