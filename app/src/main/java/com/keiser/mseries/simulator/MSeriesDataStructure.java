package com.keiser.mseries.simulator;

public class MSeriesDataStructure {
    public byte buildMajor;
    public byte bikeID;
    public byte[] rpm;
    public byte gear;
    public byte[] power;
    private long startTime = System.currentTimeMillis();

    private byte buildMinor = 0x30;
    private byte dataType = 0x00;
    private byte[] hr = {0x46, 0x05};
    private short kcal_add = 0;
    private long kcal_time_off = startTime;
    private short dst_add = 0;
    private long dst_time_off = startTime;
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
        return (System.currentTimeMillis()-kcal_time_off)*((power[0]&0xFF)+((power[1]&0xFF)<<8))/1000.0/4186.8;
    }

    private double dst() {
        short rpmv = (short) ((rpm[0]&0xFF)+((rpm[1]&0xFF)<<8));

        return (System.currentTimeMillis()-dst_time_off)*rpmv*gearrpn*Math.PI*27*2.54*60.0/3600.0*10.0/100.0/1000.0/10.0/1000.0;
    }

    public void setRpm(byte[] r) {
        dst_add +=  (short)(dst()+0.5);
        dst_time_off = System.currentTimeMillis();
        rpm = r;
    }


    public void setPower(byte[] pwr) {
        kcal_add +=  (short)(kCal()+0.5);
        kcal_time_off = System.currentTimeMillis();
        power = pwr;
    }



    private byte convertIntToByte(int value)
    {
        int newValue = value/10*16 + (value % 10);
        return (byte)newValue;
    }

    public byte[] data() {
        short kcal = (short)(kCal()+0.5+kcal_add);
        gearrpn = Math.random()*2.5+0.5;
        int dst = ((int)(dst()+0.5+dst_add))|32768;
        short time = (short) ((System.currentTimeMillis()-startTime)/1000);
        return new byte[]{buildMajor, buildMinor, dataType, bikeID, rpm[0], rpm[1], hr[0], hr[1], power[0], power[1], (byte) (kcal&0xFF), (byte) ((kcal>>8)& 0xFF), (byte) (time/60), (byte) (time%60), (byte) (dst&0xFF), (byte) ((dst>>8)&0xFF), gear};
    }

}
