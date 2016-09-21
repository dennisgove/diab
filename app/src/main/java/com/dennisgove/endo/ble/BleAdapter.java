package com.dennisgove.endo.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;

/**
 * Created by dennis on 9/11/16.
 */
public interface BleAdapter {

    boolean isEnabled();

    BluetoothLeScanner getBluetoothLeScanner();
    BluetoothDevice getRemoteDevice(String address);
    void cancelDiscovery();

}
