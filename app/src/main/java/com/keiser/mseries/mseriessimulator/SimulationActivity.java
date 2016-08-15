package com.keiser.mseries.mseriessimulator;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;


public class SimulationActivity extends AppCompatActivity {

    private static final String TAG = SimulationActivity.class.getSimpleName();

    /**
     * A global variable to let AdvertiserFragment check if the Service is running without needing
     * to start or bind to it.
     * This is the best practice method as defined here:
     * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
     */
    public static boolean running = false;

    public static final String ADVERTISING_FAILED =
            "com.example.android.bluetoothadvertisements.advertising_failed";

    public static final String ADVERTISING_FAILED_EXTRA_CODE = "failureCode";

    private BluetoothAdapter bluetoothAdapter;
    private AdvertiseCallback mAdvertiseCallback;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private BroadcastReceiver advertisingFailureReceiver;
    private KeiserDataStructure simulatedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);
        setTitle("Simulation");

        Intent intent = getIntent();
        byte bikeID = Byte.parseByte(intent.getStringExtra("BikeID"));

        String buildString= intent.getStringExtra("BuildMajor");
        String[] buildMajorSplitItems = buildString.split("\\.");
        String buildMajor = buildMajorSplitItems[0];
        String buildMinor = buildMajorSplitItems[1];
        byte build = Byte.parseByte(buildMajor);
        byte minor = Byte.parseByte(buildMinor);

        SeekBar gearSeekBar = (SeekBar)findViewById(R.id.gearSeekBar);
        gearSeekBar.setMax(255);
        gearSeekBar.setProgress(10);
        gearSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView gearTextView = (TextView)findViewById(R.id.gearTextView);
                gearTextView.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                simulatedData.gear = (byte)(seekBar.getProgress());
                TextView gearTextView = (TextView)findViewById(R.id.gearTextView);
                gearTextView.setText(String.valueOf(seekBar.getProgress()));
                mAdvertiseCallback = null;
                startAdvertising();
            }
        });

        SeekBar rpmSeekBar = (SeekBar)findViewById(R.id.rpmSeekBar);
        rpmSeekBar.setMax(1000);
        rpmSeekBar.setProgress(824);
        rpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView rpmTextView = (TextView)findViewById(R.id.rpmTextView);
                rpmTextView.setText(String.valueOf(Math.ceil(i/10.0)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                simulatedData.rpm = convertRPMToBytes(seekBar.getProgress());
                mAdvertiseCallback = null;
                startAdvertising();
            }
        });

        simulatedData = new KeiserDataStructure(build, minor,bikeID,convertRPMToBytes(rpmSeekBar.getProgress()),(byte)gearSeekBar.getProgress());

        TextView buildNumberTextView = (TextView)findViewById(R.id.buildNumberTextView);
        buildNumberTextView.setText(buildString);

        TextView bikeIDTextView = (TextView)findViewById(R.id.bikeIDTextView);
        bikeIDTextView.setText(intent.getStringExtra("BikeID"));

        TextView gearTextView = (TextView)findViewById(R.id.gearTextView);
        gearTextView.setText(String.valueOf(gearSeekBar.getProgress()));

        TextView rpmTextView = (TextView)findViewById(R.id.rpmTextView);
        rpmTextView.setText(String.valueOf(Math.ceil(rpmSeekBar.getProgress()/10)));



        advertisingFailureReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int errorCode = intent.getIntExtra(BLEAdvertiserService.ADVERTISING_FAILED_EXTRA_CODE, -1);

                String errorMessage = getString(R.string.start_error_prefix);
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errorMessage += " " + getString(R.string.start_error_already_started);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorMessage += " " + getString(R.string.start_error_too_large);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errorMessage += " " + getString(R.string.start_error_unsupported);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errorMessage += " " + getString(R.string.start_error_internal);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errorMessage += " " + getString(R.string.start_error_too_many);
                        break;
                    default:
                        errorMessage += " " + getString(R.string.start_error_unknown);
                }
            }
        };
        if (savedInstanceState == null) {
            bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()){
                    if (bluetoothAdapter.isMultipleAdvertisementSupported()) {
                        bluetoothAdapter.setName(Constants.LOCAL_NAME);
                        initialize();
                    }
                }
                else {
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetoothIntent, Constants.REQUEST_ENABLE_BT);
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }
    private void initialize() {
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                    startAdvertising();
                } else {
                }
            } else {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    if (bluetoothAdapter.isMultipleAdvertisementSupported()){
                        bluetoothAdapter.setName(Constants.LOCAL_NAME);
                        initialize();

                    }
                }
        }
    }

    /**
     * Returns Intent addressed to the {@code AdvertiserService} class.
     */
    private static Intent getServiceIntent(Context c) {
        return new Intent(c, BLEAdvertiserService.class);
    }

    private byte[] convertRPMToBytes(int currentRPM) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(currentRPM);
        byte[] results = bb.array();

        return new byte[] {results[3], results[2]};
    }

    private void startAdvertising() {
//        Intent intent = getServiceIntent(this);
//        intent.getExtras();
//        startService(getServiceIntent(this));
        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();
            mAdvertiseCallback = new SampleAdvertiseCallback();

            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(settings, data,
                        mAdvertiseCallback);
            }
        }
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private AdvertiseData buildAdvertiseData() {

        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         *  This includes everything put into AdvertiseData including UUIDs, device info, &
         *  arbitrary service or manufacturer data.
         *  Attempting to send packets over this limit will result in a failure with error code
         *  AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         *  onStartFailure() method of an AdvertiseCallback implementation.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.addManufacturerData(3, simulatedData.data());

        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());

        return dataBuilder.build();
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTimeout(0);
        settingsBuilder.setConnectable(false);
        return settingsBuilder.build();
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(TAG, "Advertising failed");
            sendFailureIntent(errorCode);


        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "Advertising successfully started");
        }
    }

    /**
     * Builds and sends a broadcast intent indicating Advertising has failed. Includes the error
     * code as an extra. This is intended to be picked up by the {@code AdvertiserFragment}.
     */
    private void sendFailureIntent(int errorCode){
        Intent failureIntent = new Intent();
        failureIntent.setAction(ADVERTISING_FAILED);
        failureIntent.putExtra(ADVERTISING_FAILED_EXTRA_CODE, errorCode);
        sendBroadcast(failureIntent);
    }

}
