package io.wkna.sdp.structures;

import fr.devnied.bitlib.BitUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static io.wkna.sdp.DemoUtils.*;

/*
* https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/sp/src/game/shared/usercmd.cpp
* */
public class UserCmdInfo {
    private byte[] rawBuffer;

    private int commandNumber = -1;
    private int tickCount = -1;
    private float[] viewAngles = {-1, -1, -1};
    private float sideMove = -1;
    private float forwardMove = -1;
    private float upMove = -1;
    private int buttons = -1;
    private int impulse = -1;
    private int weaponSelect = -1;
    private int weaponSubtype = -1;
    private short mouseDx = -1;
    private short mouseDy = -1;

    public UserCmdInfo(SeekableByteChannel sbc, int size) throws IOException {
        //TODO: Handle this better
        rawBuffer = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
        sbc.read(buffer);
        //System.out.println("\t\t\tfulldata: " + new String(rawBuffer));

        int bitsize = size * 8;
        BitUtils bitSet = new BitUtils(rawBuffer);
        if(bitSet.getCurrentBitIndex() + 33 < bitsize && bitSet.getNextBoolean()){
            commandNumber = (int) bitSet.getNextLong(32);
            //System.out.println("\t\t\tcommandNumber: " + commandNumber);
        }
        if(bitSet.getCurrentBitIndex() + 33 < bitsize && bitSet.getNextBoolean()){
            tickCount = (int) bitSet.getNextLong(32);
            //System.out.println("\t\t\ttickCount: " + tickCount);
        }
        for(int i = 0; i < 3; i++){
            if(bitSet.getCurrentBitIndex() + 33 < bitsize && bitSet.getNextBoolean()){
                viewAngles[i] = Float.intBitsToFloat((int) bitSet.getNextLong(32));
                //System.out.println("\t\t\tviewAngle " + i + ": " + viewAngles[i]);
            }
        }
        if(bitSet.getCurrentBitIndex() + 33 < bitsize && bitSet.getNextBoolean()){
            forwardMove = Float.intBitsToFloat((int) bitSet.getNextLong(32));
            //System.out.println("\t\t\tforwardMove: " + forwardMove);
        }
        if(bitSet.getCurrentBitIndex() + 33 < bitsize && bitSet.getNextBoolean()){
            sideMove = Float.intBitsToFloat((int) bitSet.getNextLong(32));
            //System.out.println("\t\t\tsideMove: " + sideMove);
        }
        if(bitSet.getCurrentBitIndex() + 33 < bitsize && bitSet.getNextBoolean()){
            upMove = Float.intBitsToFloat((int) bitSet.getNextLong(32));
            //System.out.println("\t\t\tupMove: " + upMove);
        }
        if(bitSet.getCurrentBitIndex() + 33 < bitsize && bitSet.getNextBoolean()){
            buttons = (int) bitSet.getNextLong(32);
            //System.out.println("\t\t\tbuttons: " + buttons);
        }
        if(bitSet.getCurrentBitIndex() + 9 < bitsize && bitSet.getNextBoolean()){
            impulse = (int) bitSet.getNextLong(8);
            //System.out.println("\t\t\timpulse: " + impulse);
        }
        if(bitSet.getCurrentBitIndex() + 12 < bitsize && bitSet.getNextBoolean()){
            weaponSelect = (int) bitSet.getNextLong(11);
            //System.out.println("\t\t\tweaponSelect: " + weaponSelect);
            if(bitSet.getCurrentBitIndex() + 7 < bitsize && bitSet.getNextBoolean()){
                weaponSubtype = (int) bitSet.getNextLong(6);
                //System.out.println("\t\t\tweaponSubtype: " + weaponSubtype);
            }
        }
        if(bitSet.getCurrentBitIndex() + 17 < bitsize && bitSet.getNextBoolean()){
            mouseDx = (short) bitSet.getNextLong(16);
            //System.out.println("\t\t\tmouseDx: " + mouseDx);
        }
        if(bitSet.getCurrentBitIndex() + 17 < bitsize && bitSet.getNextBoolean()){
            mouseDy = (short) bitSet.getNextLong(16);
            //System.out.println("\t\t\tmouseDy: " + mouseDy);
        }
    }

    // TODO: Store/retrieve this data better
    public int getDataSizeFlattened() {
        return rawBuffer.length;
    }

    public int write(ByteBuffer dst, int remaining, long offset) {
        return writeRawData(rawBuffer, dst, remaining, (int) offset);
    }
}
