package io.wkna.sdp.messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

public abstract class DemoMessage {
    private int tick;
    private boolean alignmentByte;

    public DemoMessage(SeekableByteChannel sbc, boolean alignmentByte) throws IOException {
        this.tick = readInt(sbc);
        this.alignmentByte = alignmentByte;
        System.out.println("\t\ttick: " + tick);
        if(alignmentByte){
            ByteBuffer buffer = ByteBuffer.allocate(1);
            sbc.read(buffer);
            System.out.println("\t\talignmentbyte");
        }
    }

    public long getFlattenedSize() {
        if(alignmentByte) {
            return 1 + 4 + 1 + getDataSize();
        } else {
            return 1 + 4 + getDataSize();
        }
    }

    abstract long getDataSize();

    public int write(ByteBuffer dst, int remaining, int offset) {
        int writeSize;
        int totalWrites = 0;

        if(alignmentByte) {
            switch (offset) {
                case 0:
                    if ((writeSize = writeByte(getMessageType(), dst, remaining - totalWrites)) == 0) {
                        return totalWrites;
                    }
                    totalWrites += writeSize;
                case 1:
                    if ((writeSize = writeInt(tick, dst, remaining - totalWrites)) == 0) {
                        return totalWrites;
                    }
                    totalWrites += writeSize;
                case 1+4:
                    if ((writeSize = writeByte((byte) 0x00, dst, remaining - totalWrites)) == 0) {
                        return totalWrites;
                    }
                    totalWrites += writeSize;
                case 1+4+1:
                    return totalWrites + writeData(dst, remaining - totalWrites, 0);
                default:
                    if(offset > 1+4+1) {
                        return totalWrites + writeData(dst, remaining - totalWrites, offset - (1+4+1));
                    } else {
                        throw new IllegalArgumentException("Demo message write offset must be on a value boundary.");
                    }
            }
        } else {
            switch (offset) {
                case 0:
                    if ((writeSize = writeByte(getMessageType(), dst, remaining - totalWrites)) == 0) {
                        return totalWrites;
                    }
                    totalWrites += writeSize;
                case 1:
                    if ((writeSize = writeInt(tick, dst, remaining - totalWrites)) == 0) {
                        return totalWrites;
                    }
                    totalWrites += writeSize;
                case 1+4:
                    return totalWrites + writeData(dst, remaining - totalWrites, 0);
                default:
                    if(offset > 1+4) {
                        return totalWrites + writeData(dst, remaining - totalWrites, offset - (1+4+1));
                    } else {
                        throw new IllegalArgumentException("Demo message write offset must be on a value boundary.");
                    }
            }
        }
    }


    public abstract int writeData(ByteBuffer dst, int remaining, int offset);

    public abstract byte getMessageType();
}
