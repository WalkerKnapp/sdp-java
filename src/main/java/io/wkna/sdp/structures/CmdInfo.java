package io.wkna.sdp.structures;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

public class CmdInfo {
    // TODO: Analyze the usage of this parameter
    private int flags;
    // The X-Y-Z coordinates of the player relevant to the packet
    private float[] viewOrigin;
    // The Y-P-R rotation of the player relevant to the packet
    private float[] viewAngles;
    // TODO: Analyze the usage of this parameter
    private float[] localViewAngles;
    // TODO: Analyze the usage of this parameter
    private float[] viewOrigin2;
    // TODO: Analyze the usage of this parameter
    private float[] viewAngles2;
    // TODO: Analyze the usage of this parameter
    private float[] localViewAngles2;

    public CmdInfo(SeekableByteChannel sbc) throws IOException {
        flags = readInt(sbc);
        //System.out.println("\t\t\tflags: " + flags);
        viewOrigin = readFloatArray(sbc, 3);
        //System.out.println("\t\t\tviewOrigin: " + floatsToString(viewOrigin));
        viewAngles = readFloatArray(sbc, 3);
        //System.out.println("\t\t\tviewAngles: " + floatsToString(viewAngles));
        localViewAngles = readFloatArray(sbc, 3);
        //System.out.println("\t\t\tlocalViewAngles: " + floatsToString(localViewAngles));
        viewOrigin2 = readFloatArray(sbc, 3);
        //System.out.println("\t\t\tviewOrigin2: " + floatsToString(viewOrigin2));
        viewAngles2 = readFloatArray(sbc, 3);
        //System.out.println("\t\t\tviewAngles2: " + floatsToString(viewAngles2));
        localViewAngles2 = readFloatArray(sbc, 3);
        //System.out.println("\t\t\tlocalViewAngles2: " + floatsToString(localViewAngles2));
    }

    public int getDataSizeFlattened() {
        return 4+12+12+12+12+12+12;
    }

    public int write(ByteBuffer dst, int remaining, int offset) {
        int writeSize;
        int totalWrites = 0;

        switch (offset) {
            case 0:
                if ((writeSize = writeInt(flags, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4:
                if ((writeSize = writeFloatArray(viewOrigin, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4+12:
                if ((writeSize = writeFloatArray(viewAngles, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4+12+12:
                if ((writeSize = writeFloatArray(localViewAngles, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4+12+12+12:
                if ((writeSize = writeFloatArray(viewOrigin2, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4+12+12+12+12:
                if ((writeSize = writeFloatArray(viewAngles2, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
            case 4+12+12+12+12+12:
                if ((writeSize = writeFloatArray(localViewAngles2, dst, remaining - totalWrites)) == 0) {
                    return totalWrites;
                }
                totalWrites += writeSize;
                return totalWrites;
            default:
                throw new IllegalArgumentException("CmdInfo offset must fall on a value boundary.");
        }
    }
}
