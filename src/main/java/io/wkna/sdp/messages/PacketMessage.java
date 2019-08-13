package io.wkna.sdp.messages;

import io.wkna.sdp.structures.CmdInfo;
import io.wkna.sdp.structures.NetData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Scanner;

import static io.wkna.sdp.DemoUtils.*;

public class PacketMessage extends DemoMessage {
    private CmdInfo[] packetInfo;
    private int inSequence;
    private int outSequence;
    private int dataSize;
    private NetData data;

    public PacketMessage(SeekableByteChannel sbc, boolean alignmentByte, int mscc) throws IOException {
        super(sbc, alignmentByte);
        packetInfo = new CmdInfo[mscc];
        for(int i = 0; i < mscc; i++){
            System.out.println("\t\t=== Packet Info:");
            packetInfo[i] = new CmdInfo(sbc);
        }
        inSequence = readInt(sbc);
        System.out.println("\t\tinSequence: " + inSequence);
        outSequence = readInt(sbc);
        System.out.println("\t\toutSequnce: " + outSequence);
        dataSize = readInt(sbc);
        System.out.println("\t\tdataSize: " + dataSize);
        if(dataSize > 0) {
            System.out.println("\t\t=== NetData:");
            data = new NetData(sbc, dataSize);
        }
    }

    @Override
    long getDataSize() {
        return (76 * packetInfo.length) + 4 + 4 + 4 + dataSize;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, int offset) {
        int writeSize;
        int totalWrites = 0;

        boolean offsetUsed = offset == 0;

        for(int i = 0; i < packetInfo.length; i++) {
            if(offsetUsed || offset >= (i * 76) && offset < ((i * 76) + 76)) {
                if ((writeSize = packetInfo[i].write(dst, remaining - totalWrites, offsetUsed ? 0 : offset - (i * 76))) != packetInfo[i].getDataSizeFlattened()) {
                    return totalWrites;
                }
                totalWrites += writeSize;
                offsetUsed = true;
            }
        }

        if(offsetUsed || offset == (packetInfo.length * 76)) {
            if ((writeSize = writeInt(inSequence, dst, remaining - totalWrites)) == 0) {
                return totalWrites;
            }
            totalWrites += writeSize;
            offsetUsed = true;
        }
        if(offsetUsed || offset == (packetInfo.length * 76) + 4) {
            if ((writeSize = writeInt(outSequence, dst, remaining - totalWrites)) == 0) {
                return totalWrites;
            }
            totalWrites += writeSize;
            offsetUsed = true;
        }
        if(offsetUsed || offset == (packetInfo.length * 76) + 4 + 4) {
            if ((writeSize = writeInt(dataSize, dst, remaining - totalWrites)) == 0) {
                return totalWrites;
            }
            totalWrites += writeSize;
            offsetUsed = true;
        }
        if(offsetUsed || offset >= (packetInfo.length * 76) + 4 + 4 + 4) {
            int dataOffset = 0;
            if(offset >= (packetInfo.length * 76) + 4 + 4 + 4) {
                dataOffset = offset - ((packetInfo.length * 76) + 4 + 4 + 4);
            }

            writeSize = data.write(dst, remaining - totalWrites, dataOffset);
            totalWrites += writeSize;
            if (totalWrites == remaining) {
                return totalWrites;
            }
            offsetUsed = true;
        }

        if(!offsetUsed) {
            throw new IllegalArgumentException("Packet message write offset must be on a value boundary.");
        }

        return totalWrites;
    }

    @Override
    public byte getMessageType() {
        return 2;
    }
}
