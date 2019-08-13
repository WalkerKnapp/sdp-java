package io.wkna.sdp.messages;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

public class SignOnMessage extends PacketMessage {
    public SignOnMessage(SeekableByteChannel sbc, boolean alignmentByte, int mscc) throws IOException {
        super(sbc, alignmentByte, mscc);
    }

    @Override
    public byte getMessageType() {
        return 1;
    }
}
