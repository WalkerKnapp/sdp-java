package io.wkna.sdp;

import io.wkna.sdp.messages.DemoMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;

public class DemoWriterChannel implements SeekableByteChannel {

    private SourceDemo demo;

    private long amtHeaderRead = 0;
    private int currentMessage = 0;
    private long amtCurrentMessageRead = 0;

    private boolean open = true;

    DemoWriterChannel(SourceDemo demo) {
        this.demo = demo;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if(!open) {
            throw new ClosedChannelException();
        }

        ArrayList<DemoMessage> messages = demo.getMessages();

        if(amtHeaderRead == demo.getHeaderFlattenedSize() && currentMessage >= messages.size()) {
            return -1;
        }

        int read;
        int totalRead = 0;
        int remaining = dst.remaining();

        // Try to write as much of the header as possible
        if(amtHeaderRead < demo.getHeaderFlattenedSize()) {
            read = demo.writeHeader(dst, remaining, amtHeaderRead);
            remaining -= read;
            amtHeaderRead += read;
            totalRead += read;

            if(amtHeaderRead < demo.getHeaderFlattenedSize()) {
                dst.position(totalRead);
                return totalRead;
            }
        }

        // Try to write as many messages as possible
        while(remaining > 0 && currentMessage < messages.size()) {
            DemoMessage message = messages.get(currentMessage);

            read = message.write(dst, remaining, amtCurrentMessageRead);
            remaining -= read;
            amtCurrentMessageRead += read;
            totalRead += read;

            if(amtCurrentMessageRead < message.getFlattenedSize()) {
                dst.position(totalRead);
                return totalRead;
            }
            amtCurrentMessageRead = 0;
            currentMessage++;
        }

        dst.position(totalRead);
        return totalRead;
    }

    @Override
    public long position() {
        long pos = amtHeaderRead;
        for(int i = 0; i < currentMessage; i++) {
            pos += demo.getMessages().get(i).getFlattenedSize();
        }
        pos += amtCurrentMessageRead;
        return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
        if (newPosition < demo.getHeaderFlattenedSize()) {
            amtHeaderRead = newPosition;
            currentMessage = 0;
            amtCurrentMessageRead = 0;
        } else {
            amtHeaderRead = demo.getHeaderFlattenedSize();

            long messagePos =  newPosition - demo.getHeaderFlattenedSize();
            for(int i = 0; i < demo.getMessages().size(); i++) {
                if(messagePos < demo.getMessages().get(i).getFlattenedSize()) {
                    currentMessage = i;
                    amtCurrentMessageRead = demo.getMessages().get(i).getFlattenedSize() - messagePos;
                    break;
                } else {
                    messagePos -= demo.getMessages().get(i).getFlattenedSize();
                }
            }
        }

        return this;
    }

    @Override
    public long size() {
        long remaining = demo.getHeaderFlattenedSize() - amtHeaderRead;

        ArrayList<DemoMessage> messages = demo.getMessages();
        remaining += messages.get(currentMessage).getFlattenedSize() - amtCurrentMessageRead;
        for(int i = currentMessage + 1; i < messages.size(); i++) {
            remaining += messages.get(currentMessage).getFlattenedSize();
        }

        return remaining;
    }

    @Override
    public int write(ByteBuffer src) {
        throw new NonWritableChannelException();
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        throw new NonWritableChannelException();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
    }
}
