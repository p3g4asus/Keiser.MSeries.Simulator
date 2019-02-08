package com.keiser.mseries.simulator;

public class MSeriesDataStructure {
    public byte buildMajor;
    public byte bikeID;
    public byte[] rpm;
    public byte gear;
    public byte[] power;
    public short time = 0;

    private byte buildMinor = 0x30;
    private byte dataType = 0x00;
    private byte[] hr = {0x46, 0x05};
    private short kcal_add = 0;
    private short kcal_time_off = 0;
    private short dst_add = 0;
    private short dst_time_off = 0;
    private double gearrpn = 0;

    public MSeriesDataStructure(byte currentBuildMajor, int currentBuildMinor, byte currentBikeID, byte[] currentRPM, byte currentGear, byte[] currentPower) {
        buildMajor = currentBuildMajor;
        buildMinor = convertIntToByte(currentBuildMinor);
        bikeID = currentBikeID;
        rpm = currentRPM;
        gear = currentGear;
        power = currentPower;
    }

    private double kCal() {
        return (time-kcal_time_off)*((power[0]&0xFF)+((power[1]&0xFF)<<8))/4186.8;
    }

    private double dst() {
        short rpmv = (short) ((rpm[0]&0xFF)+((rpm[1]&0xFF)<<8));

        return (time-dst_time_off)*rpmv*gearrpn*Math.PI*27*2.54*60.0/3600.0*10.0/100.0/1000.0;
    }

    public void setRpm(byte[] r) {
        dst_add +=  (short)(dst()+0.5);
        dst_time_off = time;
        rpm = r;
    }


    public void setPower(byte[] pwr) {
        kcal_add +=  (short)(kCal()+0.5);
        kcal_time_off = time;
        power = pwr;
    }



    private byte convertIntToByte(int value)
    {
        int newValue = value/10*16 + (value % 10);
        return (byte)newValue;
    }

    public byte[] data() {
        short kcal = (short)(kCal()+0.5+kcal_add);
        gearrpn = Math.random()*2.5+1;
        int dst = ((int)(dst()+0.5+dst_add))|32768;
        return new byte[]{buildMajor, buildMinor, dataType, bikeID, rpm[0], rpm[1], hr[0], hr[1], power[0], power[1], (byte) (kcal&0xFF), (byte) ((kcal>>8)& 0xFF), (byte) (time/60), (byte) (time%60), (byte) (dst&0xFF), (byte) ((dst>>8)&0xFF), gear};
    }

}
