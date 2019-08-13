package io.wkna.sdp;

import io.wkna.sdp.messages.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.util.ArrayList;

import static io.wkna.sdp.DemoUtils.*;

public class SourceDemo {
    private boolean hasAlignmentByte;
    private int maxSplitScreenClients;

    private String demoFileStamp;
    private int demoProtocol;
    private int networkProtocol;
    private String serverName;
    private String clientName;
    private String mapName;
    private String gameDirectory;
    private float playbackTime;
    private int playbackTicks;
    private int playbackFrames;
    private int signOnLength;

    private ArrayList<DemoMessage> messages = new ArrayList<>();

    public static SourceDemo parse(Path path, boolean headerOnly) throws IOException {
        SourceDemo demo = new SourceDemo();
        try (SeekableByteChannel sbc = Files.newByteChannel(path)) {
            System.out.println("===== Demo File: " + path.getFileName());
            demo.demoFileStamp = readString(sbc, 8);
            System.out.println("\tdemofilestamp: " + demo.demoFileStamp);
            demo.demoProtocol = readInt(sbc);
            System.out.println("\tdemoProtocol: " + demo.demoProtocol);
            demo.networkProtocol = readInt(sbc);
            System.out.println("\tnetworkProtocol: " + demo.networkProtocol);
            demo.serverName = readString(sbc, 260).trim();
            System.out.println("\tserverName: " + demo.serverName);
            demo.clientName = readString(sbc, 260).trim();
            System.out.println("\tclientName: " + demo.clientName);
            demo.mapName = readString(sbc, 260).trim();
            System.out.println("\tmapName: " + demo.mapName);
            demo.gameDirectory = readString(sbc, 260).trim();
            System.out.println("\tgameDirectory: " + demo.gameDirectory);
            demo.playbackTime = readFloat(sbc);
            System.out.println("\tplaybackTime: " + demo.playbackTime);
            demo.playbackTicks = readInt(sbc);
            System.out.println("\tplaybackTicks: " + demo.playbackTicks);
            demo.playbackFrames = readInt(sbc);
            System.out.println("\tplaybackFrames: " + demo.playbackFrames);
            demo.signOnLength = readInt(sbc);
            System.out.println("\tsignOnLength: " + demo.signOnLength);

            if(!headerOnly){
                if(demo.demoProtocol == 4){
                    demo.hasAlignmentByte = true;
                    demo.maxSplitScreenClients = 2;
                } else {
                    demo.hasAlignmentByte = false;
                    demo.maxSplitScreenClients = 1;
                }

                messageReadLoop:
                while(sbc.isOpen()){
                    ByteBuffer buffer = ByteBuffer.allocate(1);
                    if(sbc.read(buffer) != 1){
                        break;
                    }
                    byte messageType = buffer.get(0);
                    System.out.println("\t=== Message Type: " + messageType);

                    switch (messageType){
                        case 1: demo.messages.add(new SignOnMessage(sbc, demo.hasAlignmentByte, demo.maxSplitScreenClients)); break;
                        case 2: demo.messages.add(new PacketMessage(sbc, demo.hasAlignmentByte, demo.maxSplitScreenClients)); break;
                        case 3: demo.messages.add(new SyncTickMessage(sbc, demo.hasAlignmentByte)); break;
                        case 4: demo.messages.add(new ConsoleCmdMessage(sbc, demo.hasAlignmentByte)); break;
                        case 5: demo.messages.add(new UserCmdMessage(sbc, demo.hasAlignmentByte)); break;
                        case 6: demo.messages.add(new DataTablesMessage(sbc, demo.hasAlignmentByte)); break;
                        case 7: demo.messages.add(new StopMessage(sbc, demo.hasAlignmentByte)); break messageReadLoop;
                        case 8:
                            if(demo.demoProtocol == 4){
                                demo.messages.add(new CustomDataMessage(sbc, demo.hasAlignmentByte));
                            } else {
                                demo.messages.add(new StringTablesMessage(sbc, demo.hasAlignmentByte, false));
                            }
                            break;
                        case 9: demo.messages.add(new StringTablesMessage(sbc, demo.hasAlignmentByte, true)); break;
                        default:
                            break;
                    }
                }
            }
        }
        return demo;
    }

    public String getDemoFileStamp() {
        return demoFileStamp;
    }

    public int getDemoProtocol() {
        return demoProtocol;
    }

    public int getNetworkProtocol() {
        return networkProtocol;
    }

    public String getServerName() {
        return serverName;
    }

    public String getClientName() {
        return clientName;
    }

    public String getMapName() {
        return mapName;
    }

    public String getGameDirectory() {
        return gameDirectory;
    }

    public float getPlaybackTime() {
        return playbackTime;
    }

    public int getPlaybackTicks() {
        return playbackTicks;
    }

    public int getPlaybackFrames() {
        return playbackFrames;
    }

    public int getSignOnLength() {
        return signOnLength;
    }

    public ArrayList<DemoMessage> getMessages() {
        return messages;
    }

    public DemoWriterChannel createWriter() {
        return new DemoWriterChannel(this);
    }

    long getHeaderFlattenedSize() {
        return 8 + 4 + 4 + 260 + 260 + 260 + 260 + 4 + 4 + 4 + 4;
    }

    int writeHeader(ByteBuffer buffer, int remaining, int offset) {

        int writeSize;
        int totalWrites = 0;

        switch (offset) {
            case 0:
                if((writeSize = writeString(demoFileStamp, buffer, remaining - totalWrites, 8)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8:
                if((writeSize = writeInt(demoProtocol, buffer, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4:
                if((writeSize = writeInt(networkProtocol, buffer, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4+4:
                if((writeSize = writeString(serverName, buffer, remaining - totalWrites, 260)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4+4+260:
                if((writeSize = writeString(clientName, buffer, remaining - totalWrites, 260)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4+4+260+260:
                if((writeSize = writeString(mapName, buffer, remaining - totalWrites, 260)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4+4+260+260+260:
                if((writeSize = writeString(gameDirectory, buffer, remaining - totalWrites, 260)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4+4+260+260+260+260:
                if((writeSize = writeFloat(playbackTime, buffer, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4+4+260+260+260+260+4:
                if((writeSize = writeInt(playbackTicks, buffer, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4+4+260+260+260+260+4+4:
                if((writeSize = writeInt(playbackFrames, buffer, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 8+4+4+260+260+260+260+4+4+4:
                if((writeSize = writeInt(signOnLength, buffer, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
                return totalWrites;
            default:
                throw new IllegalArgumentException("Header read offset must be on a value boundary.");
        }
    }

    public static void main(String[] args) throws IOException {
        Path copyPath = Paths.get("H:\\Portal 2\\Rendering\\copy.dem");
        SourceDemo demo = parse(Paths.get("H:\\Portal 2\\Rendering\\BombFlings_2268_spidda.dem"), false);

        Files.deleteIfExists(copyPath);
        Files.createFile(copyPath);
        try (FileChannel channel = FileChannel.open(copyPath, StandardOpenOption.WRITE);
            DemoWriterChannel reader = demo.createWriter()) {
            channel.transferFrom(reader, 0, reader.remaining());
        }
    }

}
