package com.keiser.mseries.mseriessimulator;

import android.bluetooth.le.AdvertiseCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;


public class SimulationActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver advertisingFailureReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);
        setTitle("Simulation");
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
                        startAdvertising();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    if (bluetoothAdapter.isMultipleAdvertisementSupported()){
                        bluetoothAdapter.setName(Constants.LOCAL_NAME);
                        startAdvertising();

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

    private void startAdvertising() {
        Intent intent = getServiceIntent(this);
        intent.getExtras();
        startService(getServiceIntent(this));
    }

}
