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

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public long getFlattenedSize() {
        if(alignmentByte) {
            return 1 + 4 + 1 + getDataSize();
        } else {
            return 1 + 4 + getDataSize();
        }
    }

    abstract long getDataSize();

    public int write(ByteBuffer dst, int remaining, long offset) {
        int writeSize;
        int totalWrites = 0;

        boolean offsetUsed = false;
        if(offset == 0) {
            offsetUsed = true;
            if ((writeSize = writeByte(getMessageType(), dst, remaining - totalWrites)) == 0) {
                return totalWrites;
            }
            totalWrites += writeSize;
        }
        if(offsetUsed || offset == 1) {
            if ((writeSize = writeInt(tick, dst, remaining - totalWrites)) == 0) {
                return totalWrites;
            }
            totalWrites += writeSize;
        }
        if(offsetUsed || offset == 1+4) {
            if ((writeSize = writeByte((byte) 0x00, dst, remaining - totalWrites)) == 0) {
                return totalWrites;
            }
            totalWrites += writeSize;
        }
        if(offsetUsed || offset == 1+4+1) {
            return totalWrites + writeData(dst, remaining - totalWrites, 0);
        }

        if(offset > 1+4+1) {
            // TODO: GET RID OF THIS CAST TO INT
            return totalWrites + writeData(dst, remaining - totalWrites, (int) (offset - (1+4+1)));
        } else {
            throw new IllegalArgumentException("Demo message write offset must be on a value boundary.");
        }

    }


    public abstract int writeData(ByteBuffer dst, int remaining, int offset);

    public abstract byte getMessageType();
}
