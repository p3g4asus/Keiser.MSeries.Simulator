package com.keiser.mseries.simulator;

public class MSeriesDataStructure {
    public byte buildMajor;
    public byte bikeID;
    public byte[] rpm;
    public byte gear;
    public byte[] power;

    private byte buildMinor = 0x30;
    private byte dataType = 0x00;
    private byte[] hr = {0x46, 0x05};
    private byte[] kcal = {0x0D, 0x00};
    private byte minutes = 0x04;
    private byte seconds = 0x27;
    private byte[] trip = {0x01, 0x00};

    public MSeriesDataStructure(byte currentBuildMajor, byte currentBuildMinor, byte currentBikeID, byte[] currentRPM, byte currentGear, byte[] currentPower) {
        buildMajor = currentBuildMajor;
        buildMinor = currentBuildMinor;
        bikeID = currentBikeID;
        rpm = currentRPM;
        gear = currentGear;
        power = currentPower;
    }

    public byte[] data() {
        return new byte[]{buildMajor, buildMinor, dataType, bikeID, rpm[0], rpm[1], hr[0], hr[1], power[0], power[1], kcal[0], kcal[1], minutes, seconds, trip[0], trip[1], gear};
    }

}
