package io.wkna.sdp.messages;

import io.wkna.sdp.structures.UserCmdInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

public class UserCmdMessage extends DemoMessage {
    private int cmd;
    private int size;
    private UserCmdInfo data;

    public UserCmdMessage(SeekableByteChannel sbc, boolean alignmentByte) throws IOException {
        super(sbc, alignmentByte);
        cmd = readInt(sbc);
        //System.out.println("\t\tcmd: " + cmd);
        size = readInt(sbc);
        //System.out.println("\t\tsize: " + size);
        //System.out.println("\t\t=== UserCmdInfo: ");
        data = new UserCmdInfo(sbc, size);
    }

    @Override
    long getDataSize() {
        return 4+4+size;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, long offset) {
        int totalWrites = 0;

        if(offset < 4) {
            totalWrites += writeInt(cmd, dst, remaining - totalWrites, (int) offset);
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(offset < 4+4) {
            totalWrites += writeInt(size, dst, remaining - totalWrites, getOffset(offset, 4));
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(offset < 4+4+size) {
            totalWrites += data.write(dst, remaining - totalWrites, getOffsetLong(offset, 4+4));
            return totalWrites;
        }

        throw new IllegalArgumentException("ConsoleCmd Message offset must fall on a value boundary.");
    }

    @Override
    public byte getMessageType() {
        return 5;
    }
}
