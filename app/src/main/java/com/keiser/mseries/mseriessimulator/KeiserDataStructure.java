package com.keiser.mseries.mseriessimulator;

/**
 * Created by keiser on 8/12/16.
 */
public class KeiserDataStructure {
    public byte buildMajor;
    public byte bikeID;
    public byte[] rpm;
    public byte gear;

    private byte buildMinor = 0x30;
    private byte dataType = 0x00;
    private byte[] hr = {0x46,0x05};
    private byte[] power = {0x73,0x00};
    private byte[] kcal={0x0D,0x00};
    private byte minutes = 0x04;
    private byte seconds = 0x27;
    private byte[] trip = {0x01,0x00};

    public KeiserDataStructure(byte currentBuildMajor, byte currentBuildMinor, byte currentBikeID, byte[] currentRPM, byte currentGear) {
        buildMajor = currentBuildMajor;
        buildMinor = currentBuildMinor;
        bikeID = currentBikeID;
        rpm = currentRPM;
        gear = currentGear;
    }

    public byte[] data() {
        return new byte[] {buildMajor, buildMinor, dataType, bikeID, rpm[0], rpm[1], hr[0], hr[1], power[0], power[1], kcal[0], kcal[1], minutes, seconds, trip[0], trip[1], gear};
    }

}
