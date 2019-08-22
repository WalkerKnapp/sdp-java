package io.wkna.sdp.messages;

import io.wkna.sdp.structures.CmdInfo;
import io.wkna.sdp.structures.NetData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

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
            //System.out.println("\t\t=== Packet Info:");
            packetInfo[i] = new CmdInfo(sbc);
        }
        inSequence = readInt(sbc);
        //System.out.println("\t\tinSequence: " + inSequence);
        outSequence = readInt(sbc);
        //System.out.println("\t\toutSequnce: " + outSequence);
        dataSize = readInt(sbc);
        //System.out.println("\t\tdataSize: " + dataSize);
        if(dataSize > 0) {
            //System.out.println("\t\t=== NetData:");
            data = new NetData(sbc, dataSize);
        }
    }

    public NetData getNetData() {
        return data;
    }

    @Override
    long getDataSize() {
        return (76 * packetInfo.length) + 4 + 4 + 4 + dataSize;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, long offset) {
        int totalWrites = 0;

        for(int i = 0; i < packetInfo.length; i++) {
            if (offset < (i * 76) + 76) {
                totalWrites += packetInfo[i].write(dst, remaining - totalWrites, getOffsetLong(offset, i * 76));
                if(totalWrites == remaining) {
                    return totalWrites;
                }
            }
        }

        if(offset < (packetInfo.length*76)+4) {
            totalWrites += writeInt(inSequence, dst, remaining - totalWrites, getOffset(offset, packetInfo.length*76));
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(offset < (packetInfo.length*76)+4+4) {
            totalWrites += writeInt(outSequence, dst, remaining - totalWrites, getOffset(offset, (packetInfo.length*76)+4));
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(offset < (packetInfo.length*76)+4+4+4) {
            totalWrites += writeInt(dataSize, dst, remaining - totalWrites, getOffset(offset, (packetInfo.length*76)+4+4));
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(offset < (packetInfo.length*76)+4+4+4+dataSize) {
            totalWrites += data.write(dst, remaining - totalWrites, getOffsetLong(offset, (packetInfo.length*76)+4+4+4));
            return totalWrites;
        }

        throw new IllegalArgumentException("Packet message write offset must be on a value boundary.");
    }

    @Override
    public byte getMessageType() {
        return 2;
    }
}
