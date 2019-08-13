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
        System.out.println("\t\tcmd: " + cmd);
        size = readInt(sbc);
        System.out.println("\t\tsize: " + size);
        System.out.println("\t\t=== UserCmdInfo: ");
        data = new UserCmdInfo(sbc, size);
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
                if ((writeSize = writeInt(cmd, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4:
                if ((writeSize = writeInt(size, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4+4:
                if ((writeSize = data.write(dst, remaining - totalWrites, 0)) == 0) {
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
        return 5;
    }
}
