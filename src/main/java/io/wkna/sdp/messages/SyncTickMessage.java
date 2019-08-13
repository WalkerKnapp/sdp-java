package io.wkna.sdp.messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class SyncTickMessage extends DemoMessage {
    public SyncTickMessage(SeekableByteChannel sbc, boolean alignmentByte) throws IOException {
        super(sbc, alignmentByte);
    }

    @Override
    long getDataSize() {
        return 0;
    }

    @Override
    public int writeData(ByteBuffer dst, int remaining, int offset) {
        return 0;
    }

    @Override
    public byte getMessageType() {
        return 3;
    }
}
