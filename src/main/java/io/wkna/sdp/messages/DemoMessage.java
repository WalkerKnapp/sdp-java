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
        //System.out.println("\t\ttick: " + tick);
        if(alignmentByte){
            ByteBuffer buffer = ByteBuffer.allocate(1);
            sbc.read(buffer);
            //System.out.println("\t\talignmentbyte");
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
        int totalWrites = 0;

        if(offset < 1) {
            totalWrites += writeByte(getMessageType(), dst, remaining - totalWrites);
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(offset < 1+4) {
            totalWrites += writeInt(tick, dst, remaining - totalWrites, getOffset(offset, 1));
            if(totalWrites == remaining) {
                return totalWrites;
            }
        }
        if(alignmentByte) {
            if (offset < 1 + 4 + 1) {
                totalWrites += writeByte((byte) 0x00, dst, remaining - totalWrites);
                if (totalWrites == remaining) {
                    return totalWrites;
                }
            }
            if (offset < 1 + 4 + 1 + getDataSize()) {
                return totalWrites + writeData(dst, remaining - totalWrites, getOffsetLong(offset, 1 + 4 + 1));
            }
        } else {
            if (offset < 1 + 4 + getDataSize()) {
                return totalWrites + writeData(dst, remaining - totalWrites, getOffsetLong(offset, 1 + 4));
            }
        }

        throw new IllegalArgumentException("Demo message write offset must be on a value boundary. Was actually: " + offset);
    }


    abstract int writeData(ByteBuffer dst, int remaining, long offset);

    public abstract byte getMessageType();
}
